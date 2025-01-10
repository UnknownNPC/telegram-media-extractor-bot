package com.unknownnpc.media

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorService
import com.unknownnpc.media.integration.SocialMediaIntegration

trait ProcessingService:
  def publishMedia(message: String): Either[Throwable, Unit]

class ProcessingServiceImpl(extractorService: ExtractorService,
                            integrations: Seq[SocialMediaIntegration],
                            fileStorage: FileStorage = FileStorageImpl()) extends ProcessingService with StrictLogging:

  override def publishMedia(message: String): Either[Throwable, Unit] =
    for {
      extractorPayload <- extractorService.getMediaUrl(message).toRight(new RuntimeException("Cannot get media url"))
      localFile <- fileStorage.save(extractorPayload)
      _ = integrations.foreach { integration =>
        val result = integration.send(localFile, extractorPayload.extension)
        result match
          case Right(_) =>
            logger.info(s"Integration ${integration.name} completed successfully.")
          case Left(error) =>
            logger.error(s"Integration ${integration.name} failed with error: ${error.getMessage}", error)
      }
    } yield ()
