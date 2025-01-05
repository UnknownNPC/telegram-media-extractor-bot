package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.ExtractorChromeDriver.{ScreenHeight, ScreenWidth}
import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.By

import java.net.URL
import scala.jdk.CollectionConverters.*

private[extractor] class SeleniumImageInCenterExtractor(val customCookies: Seq[CustomCookie]) extends Extractor with SeleniumWebDriverLike:

  def extract(url: URL): Result =

    openPage(url, customCookies, 60_000): driver =>
      val allTagMedia = driver.findElements(By.tagName("img")).asScala.toList

      if allTagMedia.isEmpty then
        logger.info(s"No media found for <img>")
        None
      else
        logger.info(s"Found the following URLs: \n${allTagMedia.map(_.getDomAttribute("src")).mkString("\n")}")
        val screenCenterX = ScreenWidth / 2
        val screenCenterY = ScreenHeight / 2

        val centralMedia = allTagMedia.find { img =>
          val rect = img.getRect
          val imgLeft = rect.getX
          val imgTop = rect.getY
          val imgRight = imgLeft + rect.getWidth
          val imgBottom = imgTop + rect.getHeight

          screenCenterX >= imgLeft &&
            screenCenterX <= imgRight &&
            screenCenterY >= imgTop &&
            screenCenterY <= imgBottom
        }

        centralMedia match
          case Some(media) =>
            logger.info(s"The media in the center: [${media.getDomAttribute("src")}]")
            Option(media.getDomAttribute("src"))
              .map(strSrcUrl => ExtractorPayload(Set(new URL(strSrcUrl)), Extension.JPEG))
          case None =>
            logger.info(s"No media intersects with the center point.")
            None
