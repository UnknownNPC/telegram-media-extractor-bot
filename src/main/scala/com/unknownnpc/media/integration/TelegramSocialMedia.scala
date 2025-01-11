package com.unknownnpc.media.integration

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.{SendPhoto, SendVideo}
import com.pengrad.telegrambot.response.SendResponse
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.Extension
import com.unknownnpc.media.extractor.model.Extension.*

import java.nio.file.Path

private[integration] case class TelegramSocialMedia(chatId: Long, telegramBot: TelegramBot) extends SocialMediaIntegration with StrictLogging:

  override val name: String = "telegram"

  override def send(filePath: Path, extension: Extension) =
    val response: SendResponse = extension match
      case JPEG =>
        telegramBot.execute(SendPhoto(chatId, filePath.toFile))
      case MP4 | M3U8 =>
        telegramBot.execute(SendVideo(chatId, filePath.toFile))

    if response.isOk then
      logger.info(s"Payload has been sent to the telegram chat: $chatId")
      IntegrationResult(name, IntegrationStatus.Successful)
    else
      logger.error(s"Unable send payload to telegram chat: [${response.toString}]")
      IntegrationResult(name, IntegrationStatus.Failure(response.errorCode().toString))
