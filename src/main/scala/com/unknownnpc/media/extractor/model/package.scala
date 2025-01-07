package com.unknownnpc.media.extractor

package object model:
  type Result[T] = Either[ExtractorException, T]
