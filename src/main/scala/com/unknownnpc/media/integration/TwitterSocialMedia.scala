package com.unknownnpc.media.integration

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.Extension
import com.unknownnpc.media.extractor.model.Extension.*
import io.github.redouane59.twitter.TwitterClient
import io.github.redouane59.twitter.dto.tweet.TweetParameters.Media
import io.github.redouane59.twitter.dto.tweet.{MediaCategory, TweetParameters}
import io.github.redouane59.twitter.signature.TwitterCredentials

import java.nio.file.Path
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try

private[integration] case class TwitterSocialMedia(apiKey: String, apiSecret: String,
                                                   accessToken: String, accessTokenSecret: String
                                                  ) extends SocialMediaIntegration with StrictLogging:

  override val name: String = "twitter"

  override def send(filePath: Path, extension: Extension): SenderTask =
    Try {
      val credentials = TwitterCredentials.builder()
        .apiKey(apiKey)
        .apiSecretKey(apiSecret)
        .accessToken(accessToken)
        .accessTokenSecret(accessTokenSecret)
        .build()
      val twitterClient = new TwitterClient(credentials)

      val mediaCategory = extension match
        case JPEG => MediaCategory.TWEET_IMAGE
        case MP4 | M3U8 => MediaCategory.TWEET_VIDEO

      val mediaResponseOpt = mediaCategory match
        case MediaCategory.TWEET_IMAGE =>
          Option(twitterClient.uploadMedia(filePath.toFile, mediaCategory)).map(_.getMediaId)
        case MediaCategory.TWEET_VIDEO =>
          twitterClient.uploadChunkedMedia(filePath.toFile, mediaCategory).map(_.getMediaId).toScala
        case _ => throw new RuntimeException("Media category is not supported")

      val mediaId = mediaResponseOpt.getOrElse(throw new RuntimeException("Unable to create/upload media to twitter"))

      val mediaRequest = Media.builder().mediaIds(List(mediaId).asJava).build()
      val tweetParameters = TweetParameters.builder()
        .text("#nsfw #nudes #spa")
        .media(mediaRequest)
        .build()

      val tweetOpt = Option(twitterClient.postTweet(tweetParameters)).flatMap(tweet => Option(tweet.getId))
      val tweetId = tweetOpt.getOrElse(throw new RuntimeException("Failed to post tweet"))
      logger.info(s"Tweet successfully sent with ID: $tweetId")
    }.toEither
