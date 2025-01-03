package com.unknownnpc.media.extractor

import java.net.URL

case class ExtractorPayload(url: URL, extension: Extension)

enum Extension:
  case JPEG, MP4
