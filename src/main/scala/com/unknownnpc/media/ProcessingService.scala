package com.unknownnpc.media

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.{SendPhoto, SendVideo}
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorService
import com.unknownnpc.media.extractor.model.Extension.*

trait ProcessingService:
  def publishMedia(message: String): Either[Throwable, Unit]


class ProcessingServiceImpl(chatId: Long,
                            bot: TelegramBot,
                            extractorService: ExtractorService,
                            fileStorage: FileStorage = FileStorageImpl()) extends ProcessingService with StrictLogging:

  override def publishMedia(message: String): Either[Throwable, Unit] = {
    for {
      extractorPayload <- extractorService.getMediaUrl(message).toRight(new RuntimeException("Cannot get media url"))
      localFile <- fileStorage.save(extractorPayload)
      _ =
        extractorPayload.extension match
          case JPEG =>
            bot.execute(SendPhoto(chatId, localFile.toFile))
          case MP4 | M3U8 =>
            bot.execute(SendVideo(chatId, localFile.toFile))
    } yield ()
  }
