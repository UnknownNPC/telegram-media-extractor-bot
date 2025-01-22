package com.unknownnpc.media.integration

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.fs.{ImageSaveResult, SaveResult, VideoSaveResult}
import social.bigbone.MastodonClient
import social.bigbone.api.entity.data.Visibility
import social.bigbone.api.method.{FileAsMediaAttachment, MediaMethods, StatusMethods}

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

private[integration] case class MastodonSocialMedia(instanceName: String, accessToken: String)
  extends SocialMediaIntegration with StrictLogging {

  override val name: String = "mastodon"

  private val client: MastodonClient = MastodonClient.Builder(instanceName)
    .accessToken(accessToken)
    .build()

  private val mediaMethods: MediaMethods = client.media
  private val statusMethods: StatusMethods = client.statuses

  override def send(saveResult: SaveResult): IntegrationResult = {
    val mediaResult = uploadMedia(saveResult)

    mediaResult match {
      case Success(mediaId) =>
        Try {
          val response = statusMethods.postStatus(
            MastodonSocialMedia.StatusText,
            List(mediaId).asJava,
            Visibility.PUBLIC,
            null,
            true
          )
          logger.info(s"Status successfully posted with ID: ${response.execute().getId}")
          IntegrationResult(name, IntegrationStatus.Successful)
        }.recover {
          case ex: Throwable =>
            logger.error(s"Failed to post status: ${ex.getMessage}", ex)
            IntegrationResult(name, IntegrationStatus.Failure(ex.getMessage))
        }.get

      case Failure(exception) =>
        logger.error(s"Failed to upload media: ${exception.getMessage}", exception)
        IntegrationResult(name, IntegrationStatus.Failure(exception.getMessage))
    }
  }

  private def uploadMedia(saveResult: SaveResult): Try[String] = {
    Try {
      val mediaAttachment = saveResult match {
        case ImageSaveResult(path) =>
          FileAsMediaAttachment(path.toFile, "image/jpeg")
        case VideoSaveResult(path, _, _, _) =>
          FileAsMediaAttachment(path.toFile, "video/mp4")
      }
      val mediaResponse = mediaMethods.uploadMediaAsync(mediaAttachment).execute()
      mediaResponse.getId
    }
  }
}

object MastodonSocialMedia {
  val StatusText = "#nsfw #nudes #erotic #girls #spansfw"
}
