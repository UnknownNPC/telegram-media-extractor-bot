package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.ExtractorService.CustomCookie

private[extractor] class SeleniumVideoInCenterExtractor(override val customCookies: Seq[CustomCookie]) extends SeleniumMediaInCenterExtractor:
  override val tagForSearch: String = "video"
  override val extension: Extension = Extension.MP4
