package com.unknownnpc.media.extractor

private[extractor] class SeleniumImageInCenterExtractor extends SeleniumMediaInCenterExtractor:
  override val tagForSearch: String = "img"
  override val extension: Extension = Extension.JPEG
