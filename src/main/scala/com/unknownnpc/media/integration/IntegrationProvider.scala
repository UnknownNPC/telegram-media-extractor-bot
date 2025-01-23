package com.unknownnpc.media.integration

import com.pengrad.telegrambot.TelegramBot
import com.typesafe.scalalogging.StrictLogging

trait IntegrationProvider:
  def getIntegrations: Seq[SocialMediaIntegration]

case class DefaultIntegrationProvider() extends IntegrationProvider with StrictLogging:

  private val TelegramIntegrationFields =
    for {
      apiKey <- sys.env.get("TELEGRAM_TARGET_BOT_API_KEY").filter(_.nonEmpty)
      chatId <- sys.env.get("TELEGRAM_TARGET_CHAT_ID").filter(_.nonEmpty).map(_.toLong)
    } yield (apiKey, chatId)

  private val TwitterIntegrationFields =
    for {
      apiKey <- sys.env.get("TWITTER_API_KEY").filter(_.nonEmpty)
      apiSecret <- sys.env.get("TWITTER_API_SECRET").filter(_.nonEmpty)
      accessToken <- sys.env.get("TWITTER_ACCESS_TOKEN").filter(_.nonEmpty)
      accessTokenSecret <- sys.env.get("TWITTER_ACCESS_TOKEN_SECRET").filter(_.nonEmpty)
    } yield (apiKey, apiSecret, accessToken, accessTokenSecret)

  private val MastodonIntegrationFields =
    for {
      instanceName <- sys.env.get("MASTODON_INSTANCE_NAME").filter(_.nonEmpty)
      accessToken <- sys.env.get("MASTODON_ACCESS_TOKEN").filter(_.nonEmpty)
    } yield (instanceName, accessToken)

  override def getIntegrations: Seq[SocialMediaIntegration] =
    Seq(
      TelegramIntegrationFields.map((botApiKey: String, targetChatId: Long) => {
        logger.info("Telegram integration fields were found. Enabling Telegram integration")
        TelegramSocialMedia(targetChatId, new TelegramBot(botApiKey))
      }),
      TwitterIntegrationFields.map((apiKey, apiSecret, accessToken, accessTokenSecret) => {
        logger.info("Twitter integration fields were found. Enabling Twitter integration")
        TwitterSocialMedia(apiKey, apiSecret, accessToken, accessTokenSecret)
      }),
      MastodonIntegrationFields.map((instanceName, accessToken) => {
        logger.info("Mastodon integration fields were found. Enabling Mastodon integration")
        MastodonSocialMedia(instanceName, accessToken)
      })
    ).flatten
