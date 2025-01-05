package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.ExtractorChromeDriver.{ScreenHeight, ScreenWidth}
import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}

import java.net.URL
import scala.jdk.CollectionConverters.*

private[extractor] class SeleniumImageInCenterExtractor(val customCookies: Seq[CustomCookie]) extends Extractor with SeleniumWebDriverLike:

  def extract(url: URL): Result =

    openPage(url, customCookies, 60_000): driver =>
      val allTagMedia = driver.findElements(By.tagName("img")).asScala.toList
      val jsExecutor = driver.asInstanceOf[JavascriptExecutor]

      val elementsInViewPort = allTagMedia.filter(img =>
        jsExecutor.executeScript(
          """return (function(el) {
             const rect = el.getBoundingClientRect();
             const windowHeight = window.innerHeight || document.documentElement.clientHeight;
             const windowWidth = window.innerWidth || document.documentElement.clientWidth;
             return (
               rect.bottom > 0 &&
               rect.top < windowHeight &&
               rect.right > 0 &&
               rect.left < windowWidth
             );
           })(arguments[0]);""",
          img
        ).asInstanceOf[Boolean]
      )

      if elementsInViewPort.isEmpty then
        logger.info(s"No visible media found in viewport.")
        None
      else
        logger.info(s"Found the following URLs in viewport: \n${elementsInViewPort.map(_.getDomAttribute("src")).mkString("\n")}")
        val screenCenterX = ScreenWidth / 2
        val screenCenterY = ScreenHeight / 2

        val centralMedia = elementsInViewPort.minByOption { img =>
          val rect = img.getRect
          val imgCenterX = rect.getX + rect.getWidth / 2
          val imgCenterY = rect.getY + rect.getHeight / 2
          Math.sqrt(Math.pow(imgCenterX - screenCenterX, 2) + Math.pow(imgCenterY - screenCenterY, 2))
        }

        centralMedia match
          case Some(media) =>
            val src = Option(media.getDomAttribute("src")).getOrElse("unknown")
            logger.info(s"The media closest to the center: [src: $src]")
            Option(media.getDomAttribute("src"))
              .map(strSrcUrl => ExtractorPayload(Set(new URL(strSrcUrl)), Extension.JPEG))
          case None =>
            logger.info(s"No media intersects with the center point.")
            None
