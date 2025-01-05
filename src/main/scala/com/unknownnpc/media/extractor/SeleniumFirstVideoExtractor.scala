package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}

import java.net.URL
import scala.jdk.CollectionConverters.*

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
      val videos = driver.findElements(By.tagName("video")).asScala

      val jsExecutor = driver.asInstanceOf[JavascriptExecutor]

      val videosInViewport = videos.filter(img =>
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

      videosInViewport.headOption match
        case Some(media) =>
          val videoType = getVideoExtension(media)
          logger.info(s"The video on page is: [${media.getDomAttribute("src")}], extension: [$videoType]")
          Option(media.getDomAttribute("src"))
            .map(strSrcUrl => ExtractorPayload(Set(new URL(strSrcUrl)), videoType))
        case None =>
          logger.info(s"No media intersects with the center point.")
          None
