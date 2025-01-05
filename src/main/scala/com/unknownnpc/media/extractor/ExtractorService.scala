package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorPayload}

import java.net.URL

object ExtractorService:

  def apply(customCookies: Seq[CustomCookie]): ExtractorService =
    val imageInCenterExtractor = new SeleniumImageInCenterExtractor(customCookies)
    val videoInCenterExtractor = new SeleniumFirstVideoExtractor(customCookies)
    new ExtractorService(imageInCenterExtractor, videoInCenterExtractor)

class ExtractorService(imageExtractor: Extractor, videoExtractor: Extractor) extends StrictLogging:
  def getMediaUrl(pageUrl: String): Option[ExtractorPayload] =
    pageUrl.trim match
      case _ if pageUrl.startsWith("http") =>

        val url = new URL(pageUrl)

        val imageResult = imageExtractor.extract(url)
        val videoResult = videoExtractor.extract(url)

        (imageResult, videoResult) match
          case (_, Right(Some(videoUrl))) =>
            logger.info(s"Video extractor extracted video url $videoUrl")
            Some(videoUrl)
          case (Right(Some(imageUrl)), _) =>
            logger.info(s"Image extractor extracted image url $imageUrl")
            Some(imageUrl)
          case _ =>
            logger.error("Extractor service didn't find anything")
            None

      case _ =>
        logger.warn(s"Invalid page url: $pageUrl")
        None
