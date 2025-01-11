package com.unknownnpc.media.integration

enum IntegrationStatus(errorMessage: Option[String]):
  case Successful extends IntegrationStatus(None)
  case Failure(errorMessage: String) extends IntegrationStatus(Some(errorMessage))


case class IntegrationResult(integrationName: String, status: IntegrationStatus)
