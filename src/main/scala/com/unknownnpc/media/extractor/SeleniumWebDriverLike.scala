package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorChromeDriver.{*, given}
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorException, ExtractorPayload, Result}
import org.openqa.selenium.Cookie
import org.openqa.selenium.chrome.ChromeDriver as SeleniumChromeDriver
import org.slf4j.bridge.SLF4JBridgeHandler

import java.net.URL
import scala.util.{Failure, Success, Try, Using}

object SeleniumWebDriverLike:
  val DefaultPageAwaitMs = 60_000

trait SeleniumWebDriverLike extends StrictLogging:

  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  def openPage[CONTEXT](url: URL, customCookies: Seq[CustomCookie], sleepTimeout: Long)
                       (mainFn: (SeleniumChromeDriver, CONTEXT) => Option[ExtractorPayload],
                        preConfigureFn: SeleniumChromeDriver => CONTEXT = _ => ()
                       ): Result =
    val tryRunResult: Try[Option[ExtractorPayload]] = Using(ExtractorChromeDriver.getInstance()): driver =>

      val context = preConfigureFn(driver)

      driver.get("about:blank")
      driver.get(url.toString)

      if customCookies.nonEmpty then
        customCookies.foreach { customCookie =>
          val seleniumCustomCookie = new Cookie(customCookie.key, customCookie.value)
          driver.manage().addCookie(seleniumCustomCookie)
        }
        driver.navigate().refresh()

      Thread.sleep(sleepTimeout)

      mainFn(driver, context)

    tryRunResult match
      case Success(result) => Right(result)
      case Failure(exception) =>
        logger.error(s"An error occurred during extraction: ${exception.getMessage}")
        Left(ExtractorException.ProcessingError(exception.toString))
