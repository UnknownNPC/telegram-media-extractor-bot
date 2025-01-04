package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}

import java.net.URL
import java.time.Duration
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

object SeleniumMediaInCenterExtractor {
  private val ScreenHeight = 1080L
  private val ScreenWidth = 1920L
}

private[extractor] trait SeleniumMediaInCenterExtractor extends Extractor with StrictLogging:

  import SeleniumMediaInCenterExtractor.*

  private val options = new ChromeOptions() {
    addArguments("--no-sandbox")
    addArguments("--headless")
    addArguments("--disable-dev-shm-usage")
    addArguments("--enable-javascript")
    addArguments("--disable-gpu")
    addArguments(s"--window-size=$ScreenWidth,$ScreenHeight")
  }
  private val driver: WebDriver = new ChromeDriver(options)
  private val driverWait = new WebDriverWait(driver, Duration.ofSeconds(30))

  def tagForSearch: String

  def extension: Extension

  override def extract(url: URL): Result =

    val tryMedia = Try:
      driver.get(url.toString)
      driverWait.until(ExpectedConditions.presenceOfElementLocated(By.tagName(tagForSearch)))
      driver.findElements(By.tagName(tagForSearch)).asScala.toList

    tryMedia match
      case Failure(exception) =>
        Left(Exception.ProcessingError(exception.toString))
      case Success(media) =>
        logger.info(s"Found the next media: \n${media.mkString("\n")}")

        if media.isEmpty then
          logger.info(s"No media found for $tagForSearch")
          Right(None)
        else
          val screenCenterX = ScreenWidth / 2
          val screenCenterY = ScreenHeight / 2

          val nearestMedia = media.minBy { img =>
            val rect = img.getRect
            val imgCenterX = rect.getX + rect.getWidth / 2
            val imgCenterY = rect.getY + rect.getHeight / 2
            Math.sqrt(Math.pow(imgCenterX - screenCenterX, 2) + Math.pow(imgCenterY - screenCenterY, 2))
          }

          logger.info(s"Found the following closest media: [${nearestMedia.getDomAttribute("src")}]")

          val maybeUrl = Option(nearestMedia.getDomAttribute("src"))
            .map {
              case rawSrc if rawSrc.startsWith("//") => s"${url.getProtocol}:$rawSrc"
              case rawSrc if rawSrc.startsWith("/") => s"${url.getProtocol}://${url.getHost}$rawSrc"
              case rawSrc => rawSrc
            }
            .map(strSrcUrl => ExtractorPayload(new URL(strSrcUrl), extension))

          Right(maybeUrl)
