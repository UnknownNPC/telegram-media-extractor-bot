package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.SeleniumUtil.isElementVerticallyPartiallyInViewportAndHorizontallyFullyInViewport
import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.{By, JavascriptExecutor, WebElement}

import java.net.URL
import scala.jdk.CollectionConverters.*

private[extractor] class SeleniumMp4VideoExtractor(val customCookies: Seq[CustomCookie])
  extends Extractor[Option[ExtractorPayload]] with SeleniumWebDriverLike:

  override def extract(url: URL): Result[Option[ExtractorPayload]] =

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
          logger.warn(s"Unable to get video format from ${media.getDomAttribute("src")}. Using MP4 as default")
          Extension.MP4
        }

    openPage(url, customCookies): (driver, _) =>
      val videos = driver.findElements(By.tagName("video")).asScala

      val jsExecutor = driver.asInstanceOf[JavascriptExecutor]

      val videosInViewport = videos.filter(img => isElementVerticallyPartiallyInViewportAndHorizontallyFullyInViewport(jsExecutor, img))

      videosInViewport.headOption match
        case Some(media) =>
          val videoType = getVideoExtension(media)
          logger.info(s"The video on page is: [${media.getDomAttribute("src")}], extension: [$videoType]")
          Option(media.getDomAttribute("src"))
            .map(strSrcUrl => ExtractorPayload(Seq(new URL(strSrcUrl)), videoType))
        case None =>
          logger.info(s"No media intersects with the center point.")
          None
