package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorChromeDriver.{*, given}
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorException, ExtractorPayload, Result}
import org.openqa.selenium.Cookie
import org.openqa.selenium.chrome.ChromeDriver as SeleniumChromeDriver

import java.net.URL
import scala.util.{Failure, Success, Try, Using}

trait SeleniumWebDriverLike extends StrictLogging:
  def openPage(url: URL, customCookies: Seq[CustomCookie], sleepTimeout: Long)
              (fn: SeleniumChromeDriver => Option[ExtractorPayload], preConfigureFn: SeleniumChromeDriver => Unit = _ => ()): Result =
    val tryRunResult: Try[Option[ExtractorPayload]] = Using(ExtractorChromeDriver.getInstance()): driver =>

      preConfigureFn(driver)

      driver.get("about:blank")
      driver.get(url.toString)

      if customCookies.nonEmpty then
        customCookies.foreach { customCookie =>
          val seleniumCustomCookie = new Cookie(customCookie.key, customCookie.value)
          driver.manage().addCookie(seleniumCustomCookie)
        }
        driver.navigate().refresh()

      Thread.sleep(sleepTimeout)

      fn(driver)

    tryRunResult match
      case Success(result) => Right(result)
      case Failure(exception) =>
        logger.error(s"An error occurred during extraction: ${exception.getMessage}")
        Left(ExtractorException.ProcessingError(exception.toString))
