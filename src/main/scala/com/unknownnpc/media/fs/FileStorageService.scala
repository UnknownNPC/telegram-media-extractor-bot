package com.unknownnpc.media.fs

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.{Extension, ExtractorPayload}
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.{CloseableHttpClient, HttpClients}

import java.net.{URI, URL}
import java.nio.file.{Files, Path, StandardCopyOption}
import java.util.UUID
import scala.sys.process.*
import scala.util.{Try, Using}

trait FileStorage:
  def save(extractorPayload: ExtractorPayload): Either[Throwable, SaveResult]

class FileStorageImpl extends FileStorage with StrictLogging:

  override def save(extractorPayload: ExtractorPayload): Either[Throwable, SaveResult] =

    val targetFileExtension = extractorPayload.extension match
      case Extension.M3U8 => Extension.MP4
      case _ => extractorPayload.extension

    val tempFilePath = Files.createTempFile(s"download-${UUID.randomUUID().toString}", s".$targetFileExtension")

    extractorPayload.urls.toArray match
      case Array(urlOne, urlTwo)
        if urlOne.toString.toLowerCase.endsWith("m3u8") && urlTwo.toString.toLowerCase.endsWith("m3u8") =>
        for
          _ <- createVideoFromStreams(urlOne, urlTwo, tempFilePath)
          thumbnailPath <- generateThumbnail(tempFilePath)
          dimensions <- extractVideoDimensions(tempFilePath)
        yield VideoSaveResult(tempFilePath, dimensions._1, dimensions._2, thumbnailPath)

      case Array(url) =>
        httpFileDownload(url.toURI, tempFilePath).map(ImageSaveResult(_))

      case _ =>
        Left(new RuntimeException(s"Unable to process extractor URLs: $extractorPayload"))

  private def createVideoFromStreams(urlOne: URL, urlTwo: URL, outputFilePath: Path): Either[Throwable, Unit] =
    Try {
      val command = s"ffmpeg -y -i $urlOne -i $urlTwo -c:v copy -c:a aac ${outputFilePath.toString}"
      logger.info(s"Trying to create video via the following command: [$command]")
      val exitCode = command.!
      if exitCode != 0 then throw new RuntimeException("Video file creation error")
      logger.info("Video file creation successful!")
    }.toEither

  private def generateThumbnail(videoFilePath: Path): Either[Throwable, Path] =
    Try {
      val thumbnailPath = Files.createTempFile(s"thumbnail-${UUID.randomUUID().toString}", ".jpeg")
      val command = s"ffmpeg -y -i ${videoFilePath.toString} -ss 00:00:02.000 -vframes 1 ${thumbnailPath.toString}"
      logger.info(s"Generating thumbnail via command: [$command]")
      val exitCode = command.!
      if exitCode != 0 then throw new RuntimeException("Thumbnail creation error")
      logger.info("Thumbnail created successfully!")
      thumbnailPath
    }.toEither

  private def extractVideoDimensions(videoFilePath: Path): Either[Throwable, (Int, Int)] =
    Try {
      val command = s"ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=p=0 ${videoFilePath.toString}"
      logger.info(s"Extracting video dimensions via command: [$command]")
      val dimensionsOutput = command.!!
      val Array(width, height) = dimensionsOutput.trim.split(",").map(_.toInt)
      logger.info(s"Video dimensions extracted: width=$width, height=$height")
      (width, height)
    }.toEither

  private def httpFileDownload(uri: URI, outputFilePath: Path): Either[Throwable, Path] =
    Try {
      logger.info(s"Starting file downloading to the $outputFilePath")
      val client: CloseableHttpClient = HttpClients.createDefault()
      Using.resource(client) { httpClient =>
        val request = new HttpGet(uri)
        httpClient.execute(request, response => {
          val entity = response.getEntity
          Using.resource(entity.getContent) { inputStream =>
            Files.copy(inputStream, outputFilePath, StandardCopyOption.REPLACE_EXISTING)
          }
          logger.info("File download completed")
          outputFilePath
        })
      }
    }.toEither
