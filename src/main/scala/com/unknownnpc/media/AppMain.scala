package com.unknownnpc.media

import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorService
import com.unknownnpc.media.extractor.ExtractorService.CustomCookie

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}
import scala.jdk.CollectionConverters.*

object AppMain extends StrictLogging:

  private val TelegramBotApiKey = sys.env.getOrElse("TELEGRAM_BOT_API_KEY", throw new RuntimeException("Please set TELEGRAM_BOT_API_KEY env var"))
  private val TargetChatId = sys.env.getOrElse("TELEGRAM_TARGET_CHAT_ID", throw new RuntimeException("Please set TELEGRAM_TARGET_CHAT_ID env var")).toLong
  private val TelegramValidUserNames = sys.env.getOrElse("TELEGRAM_VALID_USERS", throw new RuntimeException("Please set TELEGRAM_VALID_USERS env var")).split(",")
  private val WebClientCookiesRawPair: Array[String] = sys.env.get("WEB_CLIENT_COOKIES").map(_.split(";")).getOrElse(Array.empty)
  private val WebClientCookies = WebClientCookiesRawPair.map(cookiePair => cookiePair.split(":") match
    case Array(key, value) => CustomCookie(key, value)
    case _ => throw new RuntimeException(s"Invalid key:par format: $cookiePair")
  )

  private val TelegramBot = new TelegramBot(TelegramBotApiKey)

  private val processingService = new ProcessingServiceImpl(
    TargetChatId,
    TelegramBot,
    ExtractorService(WebClientCookies)
  )

  def main(args: Array[String]): Unit =
    TelegramBot.setUpdatesListener(updates =>
      for (update <- updates.asScala)
        Option(update.message) match
          case Some(message) if TelegramValidUserNames.contains(message.from().username()) =>
            logger.info(s"Got the following message: ${message.text()} from the verified user. Processing...")
            val response = processingService.publishMedia(message.text())
            logger.info(s"Message processing has been done: $response")
          case Some(message) =>
            logger.warn(s"Got the following message: ${message.text()} from unknown user: ${message.from().username()}")
          case None => logger.error(s"Message body is empty: ${update.updateId()}")
      UpdatesListener.CONFIRMED_UPDATES_ALL
    )
    logger.info("App has been started")
    Await.result(Promise[Unit]().future, Duration.Inf)
