package com.unknownnpc.media.integration

import com.pengrad.telegrambot.TelegramBot
import com.typesafe.scalalogging.StrictLogging

trait IntegrationProvider:
  def getIntegrations: Seq[SocialMediaIntegration]

case class DefaultIntegrationProvider() extends IntegrationProvider with StrictLogging:

  private val TelegramIntegrationFields =
    for {
      apiKey <- sys.env.get("TELEGRAM_TARGET_BOT_API_KEY")
      chatId <- sys.env.get("TELEGRAM_TARGET_CHAT_ID").map(_.toLong)
    } yield (apiKey, chatId)

  private val TwitterIntegrationFields =
    for {
      apiKey <- sys.env.get("TWITTER_API_KEY")
      apiSecret <- sys.env.get("TWITTER_API_SECRET")
      accessToken <- sys.env.get("TWITTER_ACCESS_TOKEN")
      accessTokenSecret <- sys.env.get("TWITTER_ACCESS_TOKEN_SECRET")
    } yield (apiKey, apiSecret, accessToken, accessTokenSecret)

  override def getIntegrations: Seq[SocialMediaIntegration] =
    Seq(
      TelegramIntegrationFields.map((botApiKey: String, targetChatId: Long) => {
        logger.info("Telegram integration fields were found. Enabling telegram integration")
        TelegramSocialMedia(targetChatId, new TelegramBot(botApiKey))
      }),
      TwitterIntegrationFields.map((apiKey, apiSecret, accessToken, accessTokenSecret) => {
        logger.info("Twitter integration field was found. Enabling twitter integration")
        TwitterSocialMedia(apiKey, apiSecret, accessToken, accessTokenSecret)
      })
    ).flatten
