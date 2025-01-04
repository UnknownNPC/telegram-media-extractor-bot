package com.unknownnpc.media.extractor

private[extractor] class SeleniumVideoInCenterExtractor(override val customCookies: Seq[CustomCookie]) extends SeleniumMediaInCenterExtractor:
  override val tagForSearch: String = "video"
  override val extension: Extension = Extension.MP4
