package com.unknownnpc.media.extractor

package object model:
  type Result = Either[ExtractorException, Option[ExtractorPayload]]
