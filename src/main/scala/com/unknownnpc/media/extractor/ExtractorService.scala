package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorPayload}

import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

object ExtractorService:

  def apply(customCookies: Seq[CustomCookie]): ExtractorService =
    val imageInCenterExtractor = new SeleniumImageInCenterExtractor(customCookies)
    val videoInCenterExtractor = new SeleniumFirstVideoExtractor(customCookies)
    val m3u8Extractor = new SeleniumM3u8Extractor(customCookies)

    new ExtractorService(imageInCenterExtractor, videoInCenterExtractor, m3u8Extractor)

class ExtractorService(imageExtractor: Extractor, videoExtractor: Extractor, m3u8Extractor: Extractor) extends StrictLogging:
  def getMediaUrl(pageUrl: String): Option[ExtractorPayload] =
    pageUrl.trim match
      case _ if pageUrl.startsWith("http") =>

        val url = new URL(pageUrl)

        val imageResultFut = Future(imageExtractor.extract(url))
        val videoResultFut = Future(videoExtractor.extract(url))
        val m3u8ResultFut = Future(m3u8Extractor.extract(url))

        val result = Await.result(Future.sequence(Seq(imageResultFut, videoResultFut, m3u8ResultFut)), 3.minutes)

        result match
          case Seq(_, _, Right(Some(mm3u8Url))) =>
            logger.info(s"m3u8 extractor extracted video url $mm3u8Url")
            Some(mm3u8Url)
          case Seq(_, Right(Some(videoUrl)), _) =>
            logger.info(s"Video extractor extracted video url $videoUrl")
            Some(videoUrl)
          case Seq(Right(Some(imageUrl)), _, _) =>
            logger.info(s"Image extractor extracted image url $imageUrl")
            Some(imageUrl)
          case _ =>
            logger.error("Extractor service didn't find anything")
            None

      case _ =>
        logger.warn(s"Invalid page url: $pageUrl")
        None
