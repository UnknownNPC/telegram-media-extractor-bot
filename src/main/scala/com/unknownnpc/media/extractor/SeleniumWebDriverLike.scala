package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorChromeDriver.{*, given}
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorException, Result}
import org.openqa.selenium.Cookie
import org.openqa.selenium.chrome.ChromeDriver as SeleniumChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.bridge.SLF4JBridgeHandler

import java.net.URL
import scala.util.{Failure, Success, Try, Using}

object SeleniumWebDriverLike:
  val DefaultPageAwaitMs = 60_000

trait SeleniumWebDriverLike extends StrictLogging:

  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  def openPage[CONTEXT, T](url: URL, customCookies: Seq[CustomCookie], sleepTimeout: Long)
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

      waitForPage(driver, sleepTimeout)

      mainFn(driver, context)

    tryRunResult match
      case Success(result) => Right(result)
      case Failure(exception) =>
        logger.error(s"An error occurred during extraction: ${exception.getMessage}")
        Left(ExtractorException.ProcessingError(exception.toString))

  /**
   * F**ing mess of hacks for full page load detection. It should replace Thread.sleep(ms)
   */
  private def waitForPage(driver: SeleniumChromeDriver, awaitTimeout: Long): Boolean =
    val wait = new WebDriverWait(driver, java.time.Duration.ofMillis(awaitTimeout))

    def measureTime[T](stage: String)(block: => T): T =
      val start = System.currentTimeMillis()
      val result = block
      val duration = (System.currentTimeMillis() - start) / 1000.0
      logger.info(s"$stage completed in $duration seconds")
      result

    measureTime("Page load (document.readyState)") {
      wait.until { _ =>
        driver.executeScript("return document.readyState === 'complete'") == true
      }
    }

    measureTime("Image loading") {
      wait.until { _ =>
        driver.executeScript(
          """return Array.from(document.images).every(img => img.complete && img.naturalWidth > 0 && img.naturalHeight > 0)"""
        ) == true
      }
    }

    measureTime("Video loading") {
      wait.until { _ =>
        driver.executeScript(
          """return Array.from(document.querySelectorAll('video')).every(video =>
            | video.readyState >= 4 && video.videoWidth > 0 && video.videoHeight > 0)"""
            .stripMargin
        ) == true
      }
    }

    measureTime("Audio loading") {
      wait.until { _ =>
        driver.executeScript(
          """return Array.from(document.querySelectorAll('audio')).every(audio =>
            | audio.readyState >= 2)"""
            .stripMargin
        ) == true
      }
    }

    measureTime("Rendering of visible elements") {
      driver.executeScript(
        """return Array.from(document.querySelectorAll('img, video, div, span')).every(el => {
          |  const style = getComputedStyle(el);
          |  return style.visibility !== 'hidden' && style.opacity > 0 && el.getBoundingClientRect().width > 0 && el.getBoundingClientRect().height > 0;
          |})"""
          .stripMargin
      )
    }

    measureTime("Network activity completion") {
      wait.until { _ =>
        driver.executeAsyncScript(
          """const callback = arguments[arguments.length - 1];
            |let lastActivity = Date.now();
            |const observer = new MutationObserver(() => { lastActivity = Date.now(); });
            |observer.observe(document.body, { childList: true, subtree: true });
            |const checkIdle = () => {
            |  if (Date.now() - lastActivity > 3000) {
            |    observer.disconnect();
            |    callback(true);
            |  } else {
            |    setTimeout(checkIdle, 100);
            |  }
            |};
            |checkIdle();""".stripMargin
        ).asInstanceOf[Boolean]
      }
    }

    logger.info("All stages completed successfully")
    true
