package com.unknownnpc.media.extractor

import java.net.URL

trait Extractor:

  type Result = Either[Exception, Option[ExtractorPayload]]

  def extract(url: URL): Result

  enum Exception(errorMessage: String) extends RuntimeException(errorMessage):
    case NotFound extends Exception("Image not found")
    case InvalidUrl(errorMessage: String) extends Exception(errorMessage)
    case ProcessingError(errorMessage: String) extends Exception(errorMessage)
