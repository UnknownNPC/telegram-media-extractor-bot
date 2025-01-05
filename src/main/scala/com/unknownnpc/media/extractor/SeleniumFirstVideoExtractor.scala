package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.{By, WebElement}

import java.net.URL
import scala.util.Try

private[extractor] class SeleniumFirstVideoExtractor(val customCookies: Seq[CustomCookie]) extends Extractor with SeleniumWebDriverLike:
  override def extract(url: URL): Result =

    def getVideoExtension(media: WebElement): Extension =
      Option(media.getDomAttribute("type"))
        .flatMap { attr =>
          attr.split("/") match {
            case Array(_, format) => Some(format)
            case _ => None
          }
        }
        .flatMap(format => Extension.values.find(_.toString.equalsIgnoreCase(format)))
        .getOrElse {
          logger.warn(s"Unable to get video format from ${media.getDomAttribute("type")}. Using MP4 as default")
          Extension.MP4
        }

    openPage(url, customCookies, 60_000): driver =>
      val firstVideo = Try(driver.findElement(By.tagName("video"))).toOption

      firstVideo match
        case Some(media) =>
          val videoType = getVideoExtension(media)
          logger.info(s"The video on page is: [${media.getDomAttribute("src")}], extension: [$videoType]")
          Option(media.getDomAttribute("src"))
            .map(strSrcUrl => ExtractorPayload(Set(new URL(strSrcUrl)), videoType))
        case None =>
          logger.info(s"No media intersects with the center point.")
          None

