package com.unknownnpc.media

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.ExtractorService
import com.unknownnpc.media.fs.{FileStorage, FileStorageImpl}
import com.unknownnpc.media.integration.{IntegrationResult, SocialMediaIntegration}

trait ProcessingService:
  def publishMedia(message: String): Either[Throwable, Seq[IntegrationResult]]

class ProcessingServiceImpl(extractorService: ExtractorService,
                            integrations: Seq[SocialMediaIntegration],
                            fileStorage: FileStorage = FileStorageImpl()) extends ProcessingService with StrictLogging:

  override def publishMedia(message: String): Either[Throwable, Seq[IntegrationResult]] =
    for {
      extractorPayload <- extractorService.getMediaUrl(message).toRight(new RuntimeException("Cannot get media url"))
      saveResult <- fileStorage.save(extractorPayload)
      integrationRunResults = integrations.map(_.send(saveResult))
    } yield integrationRunResults
