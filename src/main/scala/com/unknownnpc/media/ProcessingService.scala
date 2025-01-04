package com.unknownnpc.media

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.{SendPhoto, SendVideo}
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.Extension.{JPEG, MP4}
import com.unknownnpc.media.extractor.{ExtractorPayload, ExtractorService}
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.{CloseableHttpClient, HttpClients}

import java.nio.file.{Files, Path, StandardCopyOption}
import scala.util.{Try, Using}

trait ProcessingService:
  def publishMedia(message: String): Either[Throwable, Unit]


class ProcessingServiceImpl(chatId: Long, bot: TelegramBot, extractorService: ExtractorService) extends ProcessingService with StrictLogging:

  override def publishMedia(message: String): Either[Throwable, Unit] = {
    for {
      extractorPayload <- extractorService.getMediaUrl(message).toRight(new RuntimeException("Cannot get media url"))
      localFile <- downloadToTempFile(extractorPayload)
      _ =
        extractorPayload.extension match
          case JPEG =>
            bot.execute(SendPhoto(chatId, localFile.toFile))
          case MP4 =>
            bot.execute(SendVideo(chatId, localFile.toFile))
    } yield ()
  }

  private def downloadToTempFile(extractorPayload: ExtractorPayload): Either[Throwable, Path] =
    Try {
      val tempFile = Files.createTempFile("download-", s".${extractorPayload.extension}")
      logger.info(s"Starting file downloading to the $tempFile")
      val client: CloseableHttpClient = HttpClients.createDefault()

      Using.resource(client) { httpClient =>
        val request = new HttpGet(extractorPayload.url.toURI)
        httpClient.execute(request, response => {
          val entity = response.getEntity
          Using.resource(entity.getContent) { inputStream =>
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING)
          }
          logger.info(s"File download has been done")
          tempFile
        })
      }
    }.toEither
