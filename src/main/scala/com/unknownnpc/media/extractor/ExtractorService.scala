package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.Extension.{JPEG, MP4}
import com.unknownnpc.media.extractor.model.MediaType.*
import com.unknownnpc.media.extractor.model.{CustomCookie, ExtractorPayload, Result}

import java.net.URL
import scala.annotation.tailrec
import scala.concurrent.duration.*

object ExtractorService:

  def apply(customCookies: Seq[CustomCookie]): ExtractorService =
    val imageInCenterExtractor = new SeleniumImageInCenterExtractor(customCookies)
    val videoInCenterExtractor = new SeleniumMp4VideoExtractor(customCookies)
    val m3u8Extractor = new SeleniumM3u8Extractor(customCookies)
    val mediaTypeRecognizer = new SeleniumMediaTypeRecognizer(customCookies)

    new ExtractorService(imageInCenterExtractor, videoInCenterExtractor, m3u8Extractor, mediaTypeRecognizer)

class ExtractorService(imageExtractor: Extractor[Option[ExtractorPayload]],
                       videoExtractor: Extractor[Option[ExtractorPayload]],
                       m3u8Extractor: Extractor[Option[ExtractorPayload]],
                       mediaTypeRecognizer: MediaTypeRecognizer) extends StrictLogging:

  def getMediaUrl(pageUrl: String): Option[ExtractorPayload] =
    pageUrl.trim match
      case _ if pageUrl.startsWith("http") =>

        def getExtractionResult(payload: Result[Option[ExtractorPayload]]) =
          payload match
            case Left(exception) =>
              logger.error(s"Unable to retrieve the data: ${exception.getMessage}")
              None
            case Right(Some(data)) =>
              logger.info(s"The data has been retrieved: $data")
              Some(data)
            case Right(None) =>
              logger.info("Unable to extract the data. Payload is empty")
              None

        val url = new URL(pageUrl)

        mediaTypeRecognizer.getMediaType(url) match
          case Mp4Url => Some(ExtractorPayload(Seq(url), MP4))
          case JpegUrl => Some(ExtractorPayload(Seq(url), JPEG))
          case U3M8Page =>
            retry(3, 5.seconds)(getExtractionResult(m3u8Extractor.extract(url)))
          case Mp4Page =>
            retry(3, 5.seconds)(getExtractionResult(videoExtractor.extract(url)))
          case ImagePage =>
            retry(3, 5.seconds)(getExtractionResult(imageExtractor.extract(url)))
          case Unknown =>
            None

      case _ =>
        logger.warn(s"Invalid page url: $pageUrl")
        None

  def retry[T](maxRetries: Int, delay: FiniteDuration)(block: => Option[T]): Option[T] =
    @tailrec
    def loop(remainingRetries: Int): Option[T] =
      block match
        case Some(value) => Some(value)
        case None if remainingRetries > 0 =>
          logger.info(s"Request will be retried. Remaining: $remainingRetries")
          Thread.sleep(delay.toMillis)
          loop(remainingRetries - 1)
        case None => None

    loop(maxRetries)
