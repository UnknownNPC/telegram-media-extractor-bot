package com.unknownnpc.media

import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.{TelegramBot, UpdatesListener}
import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorService
import com.unknownnpc.media.extractor.model.CustomCookie
import com.unknownnpc.media.integration.{DefaultIntegrationProvider, IntegrationResult, IntegrationStatus}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}
import scala.jdk.CollectionConverters.*

object AppMain extends StrictLogging:

  private val TelegramBotApiKey = sys.env.getOrElse("TELEGRAM_BOT_API_KEY", throw new RuntimeException("Please set TELEGRAM_BOT_API_KEY env var"))
  private val TelegramValidUserNames = sys.env.getOrElse("TELEGRAM_VALID_USERS", throw new RuntimeException("Please set TELEGRAM_VALID_USERS env var")).split(",")
  private val WebClientCookies = sys.env.get("WEB_CLIENT_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

  private val TelegramBot = new TelegramBot(TelegramBotApiKey)

  private val processingService = new ProcessingServiceImpl(
    ExtractorService(WebClientCookies),
    DefaultIntegrationProvider().getIntegrations
  )

  def main(args: Array[String]): Unit =
    TelegramBot.setUpdatesListener(updates =>
      for (update <- updates.asScala)
        Option(update.message) match
          case Some(message) if TelegramValidUserNames.contains(message.from().username()) =>
            logger.info(s"Got the following message: ${message.text()} from the verified user. Processing...")
            val response: Either[Throwable, Seq[IntegrationResult]] = processingService.publishMedia(message.text())
            response match {
              case Right(results) =>
                val integrationResultsFmtStr = formatIntegrationResults(results)
                TelegramBot.execute(new SendMessage(message.chat().id(), integrationResultsFmtStr))
              case Left(error) =>
                logger.error(s"Error processing message: ${message.text()} - $error")
                TelegramBot.execute(new SendMessage(message.chat().id(), s"❌ Failed to process your message: ${message.text()}."))
            }
          case Some(message) =>
            logger.warn(s"Got the following message: ${message.text()} from unknown user: ${message.from().username()}")
          case None => logger.error(s"Message body is empty: ${update.updateId()}")
      UpdatesListener.CONFIRMED_UPDATES_ALL
    )
    logger.info("App has been started")
    Await.result(Promise[Unit]().future, Duration.Inf)

  def formatIntegrationResults(results: Seq[IntegrationResult]): String = {
    results.map { result =>
      val statusMessage = result.status match {
        case IntegrationStatus.Successful => "Successful ✅"
        case IntegrationStatus.Failure(errorMessage) => s"Failure ❌: $errorMessage"
      }
      s"Integration: ${result.integrationName}\nStatus: $statusMessage"
    }.mkString("\n\n")
  }