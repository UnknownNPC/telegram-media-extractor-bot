package com.unknownnpc.media.extractor.model

import java.net.URL

case class ExtractorPayload(urls: Set[URL], extension: Extension)

enum Extension:
  case JPEG, MP4, M3U8
