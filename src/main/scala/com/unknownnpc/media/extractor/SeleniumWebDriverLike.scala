package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorChromeDriver.{*, given}
import com.unknownnpc.media.extractor.SeleniumWebDriverLike.DefaultPageAwaitMs
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorException, Result}
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriver as SeleniumChromeDriver
import org.openqa.selenium.logging.{LogEntries, LogType}
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.{Cookie, JavascriptExecutor, WebDriver}
import org.slf4j.bridge.SLF4JBridgeHandler

import java.net.URL
import java.time.Duration
import scala.jdk.CollectionConverters.*
import scala.util.*
import scala.util.boundary.break

object SeleniumWebDriverLike:
  val DefaultPageAwaitMs = 60_000

trait SeleniumWebDriverLike extends StrictLogging:

  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  def openPage[CONTEXT, T](url: URL, customCookies: Seq[CustomCookie])
                          (mainFn: (SeleniumChromeDriver, CONTEXT) => T,
                           preConfigureFn: SeleniumChromeDriver => CONTEXT = _ => ()
                          ): Result[T] =
    val tryRunResult: Try[T] = Using(ExtractorChromeDriver.getInstance()): driver =>

      val context = preConfigureFn(driver)

      driver.get(url.toString)
      if customCookies.nonEmpty then
        customCookies.foreach { customCookie =>
          val seleniumCustomCookie = new Cookie(customCookie.key, customCookie.value)
          driver.manage().addCookie(seleniumCustomCookie)
        }
        driver.navigate().refresh()

      waitForPage(driver, DefaultPageAwaitMs)

      mainFn(driver, context)

    tryRunResult match
      case Success(result) => Right(result)
      case Failure(exception) =>
        logger.error(s"An error occurred during extraction: ${exception.getMessage}")
        Left(ExtractorException.ProcessingError(exception.toString))


  /**
   * F*** mess of hacks to verify if the page has fully loaded.
   */
  private def waitForPage(driver: ChromeDriver, awaitTimeout: Long): Boolean =
    val startTime = System.currentTimeMillis()
    val endTime = startTime + awaitTimeout

    logger.info("Waiting for all XHR requests to complete...")

    def measureTime[T](actionName: String)(block: => T): (T, Long) =
      val start = System.currentTimeMillis()
      val result = block
      val elapsed = System.currentTimeMillis() - start
      logger.info(s"$actionName completed in $elapsed ms.")
      (result, elapsed)

    def waitForXHRRequests(): Boolean =
      measureTime("Waiting for XHR requests") {
        boundary:
          while System.currentTimeMillis() < endTime do
            val logs: LogEntries = driver.manage().logs().get(LogType.PERFORMANCE)
            val xhrActive = logs.getAll.asScala.exists { log =>
              val message = log.getMessage
              message.contains("Network.requestWillBeSent") && message.contains("\"type\":\"XHR\"")
            }

            if !xhrActive then
              logger.info("All XHR requests completed.")
              break(true)

            Thread.sleep(1000)

          logger.error("Timeout while waiting for XHR requests to complete.")
          false
      }._1

    def waitForPageRender(): Boolean =
      measureTime("Checking page rendering with WebDriverWait") {
        val wait = new WebDriverWait(driver, Duration.ofMillis(awaitTimeout))

        val isReadyStateComplete = wait.until((d: WebDriver) =>
          d.asInstanceOf[JavascriptExecutor].executeScript("return document.readyState === 'complete';").asInstanceOf[Boolean]
        )

        val areImagesLoaded = wait.until((d: WebDriver) =>
          d.asInstanceOf[JavascriptExecutor].executeScript(
            """return Array.from(document.images).every(img => img.complete && img.naturalWidth > 0 && img.naturalHeight > 0);"""
          ).asInstanceOf[Boolean]
        )

        val isContentVisible = wait.until((d: WebDriver) =>
          d.asInstanceOf[JavascriptExecutor].executeScript(
            """return Array.from(document.querySelectorAll('body, div, img')).some(el => {
              |  const style = getComputedStyle(el);
              |  return style.visibility !== 'hidden' && style.opacity > 0 && el.getBoundingClientRect().width > 0 && el.getBoundingClientRect().height > 0;
              |});"""
              .stripMargin
          ).asInstanceOf[Boolean]
        )

        isReadyStateComplete && areImagesLoaded && isContentVisible
      }._1

    Try {
      val (xhrSuccess, xhrTime) = measureTime("Total time for waitForXHRRequests")(waitForXHRRequests())
      val (renderSuccess, renderTime) = measureTime("Total time for isPageRendered")(waitForPageRender())

      if xhrSuccess && renderSuccess then
        logger.info(s"Page fully rendered after XHR. Total time spent: ${xhrTime + renderTime} ms.")
        true
      else
        logger.error(s"Page rendering failed. XHR time: $xhrTime ms, Render time: $renderTime ms.")
        false
    } match {
      case Success(result) => result
      case Failure(exception) =>
        logger.error(s"Error during waitForPage: ${exception.getMessage}")
        false
    }
