package com.unknownnpc.media.extractor


private[extractor] class SeleniumImageInCenterExtractor(override val customCookies: Seq[CustomCookie]) extends SeleniumMediaInCenterExtractor:
  override val tagForSearch: String = "img"
  override val extension: Extension = Extension.JPEG
