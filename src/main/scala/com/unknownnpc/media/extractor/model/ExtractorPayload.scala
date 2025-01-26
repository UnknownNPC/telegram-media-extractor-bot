package com.unknownnpc.media.extractor.model

import java.net.URL

case class ExtractorPayload(urls: Seq[URL], extension: Extension)

enum Extension:
  case JPEG, MP4, M3U8, DUAL_TRACK_MP4
