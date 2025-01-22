package com.unknownnpc.media.integration

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.{SendPhoto, SendVideo}
import com.pengrad.telegrambot.response.SendResponse
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.Extension.*
import com.unknownnpc.media.fs.{ImageSaveResult, SaveResult, VideoSaveResult}

private[integration] case class TelegramSocialMedia(chatId: Long, telegramBot: TelegramBot) extends SocialMediaIntegration with StrictLogging:

  override val name: String = "telegram"

  override def send(filePath: SaveResult): IntegrationResult =
    val response: SendResponse = filePath match
      case ImageSaveResult(path) =>
        telegramBot.execute(SendPhoto(chatId, path.toFile))
      case VideoSaveResult(path, width, height, thumbnail) =>
        val video = SendVideo(chatId, path.toFile).width(width).height(height).thumbnail(thumbnail.toFile)
        telegramBot.execute(video)

    if response.isOk then
      logger.info(s"Payload has been sent to the telegram chat: $chatId")
      IntegrationResult(name, IntegrationStatus.Successful)
    else
      logger.error(s"Unable send payload to telegram chat: [${response.toString}]")
      IntegrationResult(name, IntegrationStatus.Failure(response.errorCode().toString))
