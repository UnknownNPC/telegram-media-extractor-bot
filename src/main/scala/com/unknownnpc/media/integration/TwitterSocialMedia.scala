package com.unknownnpc.media.integration

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.Extension
import com.unknownnpc.media.extractor.model.Extension.*
import com.unknownnpc.media.fs.*
import twitter4j.*
import twitter4j.auth.AccessToken

import java.io.FileInputStream
import java.nio.file.Path
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try, Using}

private[integration] case class TwitterSocialMedia(apiKey: String, apiSecret: String,
                                                   accessToken: String, accessTokenSecret: String
                                                  ) extends SocialMediaIntegration with StrictLogging:

  private val TwitterV1Client = TwitterFactory.getSingleton
  TwitterV1Client.setOAuthConsumer(apiKey, apiSecret)
  TwitterV1Client.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret))
  private val TwitterV2Client = TwitterV2ExKt.getV2(TwitterV1Client)

  override val name: String = TwitterSocialMedia.TwitterName

  override def send(saveResult: SaveResult): IntegrationResult =
    Try {

      val mediaCategory = saveResult match
        case ImageSaveResult(_) => TwitterSocialMedia.TweetImageCategory
        case VideoSaveResult(_, _, _, _) => TwitterSocialMedia.TweetVideoCategory

      val mediaId = uploadMedia(TwitterV1Client, saveResult.path, mediaCategory).getOrElse(throw RuntimeException("Unable to create media"))

      val createTweet = TwitterV2Client.createTweet(null, null, null, Array(mediaId), Array.empty,
        null, null, null, null, null, null, TwitterSocialMedia.TweetText)
      logger.info(s"Tweet successfully sent with ID: ${createTweet.getId}")
    } match
      case Success(_) =>
        IntegrationResult(name, IntegrationStatus.Successful)
      case Failure(exception) =>
        IntegrationResult(name, IntegrationStatus.Failure(exception.getMessage))

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
