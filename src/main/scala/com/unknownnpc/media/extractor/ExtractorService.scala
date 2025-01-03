package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging

import java.net.URL

object ExtractorService:
  def apply(imageExtractor: Extractor = new SeleniumImageInCenterExtractor,
            videoExtractor: Extractor = new SeleniumVideoInCenterExtractor) =
    new ExtractorService(imageExtractor, videoExtractor)

class ExtractorService(imageExtractor: Extractor, videoExtractor: Extractor) extends StrictLogging:
  def getMediaUrl(pageUrl: String): Option[ExtractorPayload] =
    pageUrl.trim match
      case _ if pageUrl.startsWith("http") =>
        val imageInCenterExtractor = new SeleniumImageInCenterExtractor

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
