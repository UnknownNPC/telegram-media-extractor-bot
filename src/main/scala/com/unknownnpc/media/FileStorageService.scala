package com.unknownnpc.media

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.{Extension, ExtractorPayload}
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.{CloseableHttpClient, HttpClients}

import java.net.URL
import java.nio.file.{Files, Path, StandardCopyOption}
import java.util.UUID
import scala.sys.process.*
import scala.util.{Try, Using}

trait FileStorage:
  def save(extractorPayload: ExtractorPayload): Either[Throwable, Path]

class FileStorageImpl extends FileStorage with StrictLogging:
  override def save(extractorPayload: ExtractorPayload): Either[Throwable, Path] =

    val targetFileExtension = extractorPayload.extension match
      case Extension.M3U8 => Extension.MP4
      case _ => extractorPayload.extension

    val tempFilePath = Files.createTempFile(s"download-${UUID.randomUUID().toString}", s".$targetFileExtension")

    Try {
      extractorPayload.urls.toArray match
        case Array(urlOne, urlTwo)
          if urlOne.toString.toLowerCase.endsWith("m3u8") && urlTwo.toString.toLowerCase.endsWith("m3u8") =>

          logger.info("Found video streams. Trying to use ffmpeg")
          val command = s"ffmpeg -y -i $urlOne -i $urlTwo -c:v copy -c:a aac ${tempFilePath.toString}"
          logger.info(s"Trying to create video via the following command: [$command")
          val exitCode = command.!
          if exitCode == 0 then
            logger.info("Video file creation successful!")
            tempFilePath
          else
            logger.error("Unable to create video file")
            throw new RuntimeException("File video file creation error")
        case Array(url) =>
          logger.info(s"Starting file downloading to the $tempFilePath")
          val client: CloseableHttpClient = HttpClients.createDefault()

          Using.resource(client) { httpClient =>
            val request = new HttpGet(url.toURI)
            httpClient.execute(request, response => {
              val entity = response.getEntity
              Using.resource(entity.getContent) { inputStream =>
                Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING)
              }
              logger.info(s"File download has been done")
              tempFilePath
            })
          }
        case _ => throw new RuntimeException(s"Unable to process extractor URLs: $extractorPayload")
    }.toEither
