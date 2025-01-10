package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.SeleniumWebDriverLike.DefaultPageAwaitMs
import com.unknownnpc.media.extractor.model.SeleniumUtil.isElementInViewport
import com.unknownnpc.media.extractor.model.{CustomCookie, MediaType}
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.{By, WebElement}

import java.net.URL
import scala.jdk.CollectionConverters.*

trait MediaTypeRecognizer:
  def getMediaType(source: URL): MediaType

private[extractor] class SeleniumMediaTypeRecognizer(val customCookies: Seq[CustomCookie])
  extends MediaTypeRecognizer with StrictLogging with SeleniumWebDriverLike:

  override def getMediaType(source: URL): MediaType = {
    val mediaType = source.toString match
      case sourceStr if sourceStr.endsWith(".mp4") =>
        MediaType.Mp4Url
      case sourceStr if sourceStr.endsWith(".jpeg") => MediaType.JpegUrl
      case _ =>
        openPage(source, customCookies, DefaultPageAwaitMs / 2) { (driver: ChromeDriver, _) =>
          val jsExecutor = driver.asInstanceOf[org.openqa.selenium.JavascriptExecutor]
          val videos: Seq[WebElement] = driver.findElements(By.tagName("video")).asScala.toSeq
            .filter(video => video.isDisplayed && isElementInViewport(jsExecutor, video))

          if hasBlobSource(videos) then {
            MediaType.U3M8Page
          } else if (hasMp4Source(videos)) {
            MediaType.Mp4Page
          } else {
            MediaType.ImagePage
          }
        }.getOrElse(MediaType.Unknown)

    logger.info(s"URL [$source] has been detected as $mediaType")
    mediaType
  }

  private def hasBlobSource(videos: Seq[WebElement]): Boolean =
    videos.exists(video =>
      Option(video.getDomAttribute("src")).exists(_.startsWith("blob:")) ||
        video.findElements(By.tagName("source")).asScala.exists(source =>
          Option(source.getDomAttribute("src")).exists(_.startsWith("blob:"))
        )
    )

  private def hasMp4Source(videos: Seq[WebElement]): Boolean =
    videos.exists(video =>
      Option(video.getDomAttribute("src")).exists(_.endsWith(".mp4"))
    )
