package com.unknownnpc.media.integration

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.Extension
import com.unknownnpc.media.extractor.model.Extension.*
import twitter4j.*
import twitter4j.auth.AccessToken

import java.io.FileInputStream
import java.nio.file.Path
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

private[integration] case class TwitterSocialMedia(apiKey: String, apiSecret: String,
                                                   accessToken: String, accessTokenSecret: String
                                                  ) extends SocialMediaIntegration with StrictLogging:

  override val name: String = TwitterSocialMedia.TwitterName

  override def send(filePath: Path, extension: Extension): SenderTask =
    Try {
      val twitterV1Api = TwitterFactory.getSingleton
      twitterV1Api.setOAuthConsumer(apiKey, apiSecret)
      twitterV1Api.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))

      val mediaCategory = extension match
        case JPEG => TwitterSocialMedia.TweetImageCategory
        case MP4 | M3U8 => TwitterSocialMedia.TweetVideoCategory

      val mediaId = uploadMedia(twitterV1Api, filePath, mediaCategory).getOrElse(throw RuntimeException("Unable to create media"))

      val twitterV2 = TwitterV2ExKt.getV2(twitterV1Api)

      val createTweet = twitterV2.createTweet(null, null, null, Array(mediaId), Array.empty,
        null, null, null, null, null, null, TwitterSocialMedia.TweetText)
      logger.info(s"Tweet successfully sent with ID: ${createTweet.getId}")
    }.toEither

  private def uploadMedia(twitter: Twitter, filePath: Path, mediaCategory: String): Either[Throwable, Long] =

    val fileName = filePath.getFileName.toString
    val fileInputStream = new FileInputStream(filePath.toFile)

    Using(fileInputStream) { inputStreamm =>
      val mediaOpt = mediaCategory match
        case TwitterSocialMedia.TweetVideoCategory =>
          Option(twitter.tweets().uploadMediaChunked(fileName, fileInputStream))
        case TwitterSocialMedia.TweetImageCategory =>
          Option(twitter.tweets().uploadMedia(fileName, fileInputStream))

      val media = mediaOpt.getOrElse(throw RuntimeException("Unable to create media"))
      media.getMediaId
    }.toEither


object TwitterSocialMedia:
  val TwitterName = "twitter"
  val TweetImageCategory = "tweet_image"
  val TweetVideoCategory = "tweet_video"
  val TweetText = "#nsfw #nudes #erotic #girls #spansfw"
  val ProcessingSucceededState = "succeeded"
  val ProcessingInProgressState = "in_progress"
  val ProcessingFailedState = "failed"
