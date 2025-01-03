package com.unknownnpc.media

import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}
import scala.jdk.CollectionConverters.*


object AppMain extends StrictLogging:

  private val TelegramBotApiKey = sys.env.getOrElse("TELEGRAM_BOT_API_KEY", throw new RuntimeException("Please set TELEGRAM_BOT_API_KEY env var"))
  private val TargetChatId = sys.env.getOrElse("TELEGRAM_TARGET_CHAT_ID", throw new RuntimeException("Please set TELEGRAM_TARGET_CHAT_ID env var")).toLong
  private val TelegramValidUserNames = sys.env.getOrElse("TELEGRAM_VALID_USERS", throw new RuntimeException("Please set TELEGRAM_VALID_USERS env var")).split(",")

  val TelegramBot = new TelegramBot(TelegramBotApiKey)

  val processingService = new ProcessingServiceImpl(
    TargetChatId,
    TelegramBot,
    ExtractorService()
  )

  @main
  def main(): Unit =
    TelegramBot.setUpdatesListener(updates =>
      for (update <- updates.asScala)
        Option(update.message) match
          case Some(message) if TelegramValidUserNames.contains(message.from().username()) =>
            logger.info(s"Got the following message: ${message.text()} from the verified user. Processing...")
            val response = processingService.publishMedia(message.text())
            logger.info(s"Message processing has been done: $response")
          case Some(message) =>
            logger.warn(s"Got the following message: ${message.text()} from unknown user: ${message.from().username()}")
          case None => logger.error("Message body is empty")
      UpdatesListener.CONFIRMED_UPDATES_ALL
    )
    Await.result(Promise[Unit]().future, Duration.Inf)
