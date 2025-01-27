package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import io.lemonlabs.uri.Url
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.openqa.selenium.chrome.ChromeDriver

import java.net.URL
import java.util.Base64
import java.util.concurrent.CopyOnWriteArrayList
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

class SeleniumInstagramMp4Extractor(val customCookies: Seq[CustomCookie])
  extends Extractor[Option[ExtractorPayload]] with SeleniumWebDriverLike with StrictLogging:

  implicit val JsonFormats: Formats = DefaultFormats
  val preConfigureFn: ChromeDriver => CopyOnWriteArrayList[String] = driver =>
    SeleniumUtil.runUrlsListenerScanner(url => {
      url.contains(".mp4") && url.contains("bytestart") && url.contains("byteend")
    })(driver)
  val mainFn: (ChromeDriver, CopyOnWriteArrayList[String]) => Option[ExtractorPayload] = (driver, segmentedUrls) =>

    if segmentedUrls.isEmpty then
      logger.info("No segmented .mp4 links found.")
      None
    else
      logger.info(s"Segmented URLs received: ${segmentedUrls.size}")

      val uniqueUrls = segmentedUrls.asScala
        .map(urlStr => Url.parse(urlStr).removeParams("bytestart", "byteend").toJavaURI.toURL)
        .distinct
        .toSeq

      logger.info(s"Unique URLs after cleaning: ${uniqueUrls.size}")

      val classifiedUrls = uniqueUrls.flatMap(classifyUrlByEfg)

      logger.info(s"Classified URLs: ${classifiedUrls.size}")

      val bestVideo = classifiedUrls
        .filter(_.urlType == "video")
        .sortBy(_.quality)
        .headOption
        .map(_.url)

      val bestAudio = classifiedUrls
        .find(_.urlType == "audio")
        .map(_.url)

      (bestVideo, bestAudio) match
        case (Some(video), Some(audio)) =>
          logger.info(s"\nSelected video: $video\nSelected audio: $audio")
          Some(ExtractorPayload(Seq(video, audio), Extension.DUAL_TRACK_MP4))
        case _ =>
          logger.warn("No valid video or audio streams found.")
          None

  override def extract(url: URL): Result[Option[ExtractorPayload]] =
    logger.info(s"Starting extraction for URL: $url")
    openPage(url, customCookies)(mainFn, preConfigureFn)

  /**
   * Extracts and classifies media URLs based on metadata.
   *
   * @param url the input URL from which the metadata is extracted.
   * @return an optional ClassifiedUrl identifying media type and quality.
   *
   *         Classification process:
   *  1. Decode the "efg" parameter from the URL query.
   *  2. Parse the decoded data to JSON and extract the "vencode_tag" field.
   *  3. Classify the URL as either "video" or "audio":
   *    - "baseline" in "vencode_tag" indicates video quality (e.g., baseline_1 → 1).
   *    - "_qXX" patterns (e.g., _q90, _q80) are converted to quality using (100 - XX) / 10.
   *    - "audio" in "vencode_tag" marks the URL as an audio stream.
   *  4. Log details and handle decoding/parsing errors.
   */
  private def classifyUrlByEfg(url: URL): Option[ClassifiedUrl] = {
    Url.parse(url.toString).query.paramMap.get("efg").flatMap { efgValue =>
      Try {
        val decoded = String(Base64.getDecoder.decode(efgValue.head))
        logger.debug(s"Decoded efg: $decoded for URL: $url")
        (parse(decoded) \ "vencode_tag").extractOpt[String]
      } match
        case Success(Some(vencodeTag)) =>
          //video
          if vencodeTag.contains("baseline") then
            """baseline_(\d+)_v""".r.findFirstMatchIn(vencodeTag).map { matchResult =>
              val quality = matchResult.group(1).toInt
              logger.info(s"Found baseline quality: $quality for URL: $url")
              ClassifiedUrl("video", url, quality)
            }
          //video
          else if """_q(\d{2})""".r.findFirstMatchIn(vencodeTag).isDefined then
            """_q(\d{2})""".r.findFirstMatchIn(vencodeTag).map { matchResult =>
              val quality = matchResult.group(1).toInt
              val adjustedQuality = (100 - quality) / 10
              logger.info(s"Found specific qXX quality: $adjustedQuality for URL: $url")
              ClassifiedUrl("video", url, adjustedQuality)
            }
          //audio
          else if vencodeTag.contains("audio") then
            logger.info(s"Found audio stream for URL: $url")
            Some(ClassifiedUrl("audio", url, 0))
          else
            logger.warn(s"Unrecognized vencode_tag content: $vencodeTag for URL: $url")
            None

        case Success(None) =>
          logger.warn(s"Missing vencode_tag in efg for URL: $url")
          None

        case Failure(ex) =>
          logger.error(s"Failed to decode or parse efg for URL: $url", ex)
          None
    }
  }

  private case class ClassifiedUrl(urlType: String, url: URL, quality: Int)
