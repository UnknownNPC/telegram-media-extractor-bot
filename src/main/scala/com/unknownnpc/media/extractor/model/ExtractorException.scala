package com.unknownnpc.media.extractor.model

enum ExtractorException(errorMessage: String) extends RuntimeException(errorMessage):
  case NotFound extends ExtractorException("Image not found")
  case InvalidUrl(errorMessage: String) extends ExtractorException(errorMessage)
  case ProcessingError(errorMessage: String) extends ExtractorException(errorMessage)
