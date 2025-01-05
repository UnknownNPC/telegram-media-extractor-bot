package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.*

import java.net.URL
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

  def tagForSearch: String

  def extension: Extension

  def customCookies: Seq[CustomCookie]

  override def extract(url: URL): Result =

    val tryMedia = Try:
      driver.get(url.toString)
      customCookies.foreach(customCookie =>
        val seleniumCustomCookie = new Cookie(customCookie.key, customCookie.value)
        driver.manage().addCookie(seleniumCustomCookie)
      )
      driver.navigate().refresh()
      Thread.sleep(15_000) // TODO: Refactor. Otherwise, all these async images are not ready
      driver.findElements(By.tagName(tagForSearch)).asScala.toList

    tryMedia match
      case Failure(exception) =>
        Left(Exception.ProcessingError(exception.toString))
      case Success(medias) =>

        val visibleInViewport = medias.filter { element =>
          val jsExecutor = driver.asInstanceOf[JavascriptExecutor]
          val isInViewport = jsExecutor.executeScript(
            """return (function(el) {
                 const rect = el.getBoundingClientRect();
                 return (
                   rect.top >= 0 &&
                   rect.left >= 0 &&
                   rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
                   rect.right <= (window.innerWidth || document.documentElement.clientWidth)
                 );
              })(arguments[0]);""",
            element
          ).asInstanceOf[Boolean]
          isInViewport
        }

        logger.info(s"Found in the viewport: \n${visibleInViewport.mkString("\n")}")

        if visibleInViewport.isEmpty then
          logger.info(s"No media found for $tagForSearch")
          Right(None)
        else
          val screenCenterX = ScreenWidth / 2
          val screenCenterY = ScreenHeight / 2

          val nearestMedia = visibleInViewport.minBy { img =>
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
