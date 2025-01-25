package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}

import java.net.URL

private[extractor] class SeleniumImageInCenterExtractor(val customCookies: Seq[CustomCookie])
  extends Extractor[Option[ExtractorPayload]] with SeleniumWebDriverLike:

  override def extract(url: URL): Result[Option[ExtractorPayload]] =
    openPage(url, customCookies): (driver, _) =>
      SeleniumUtil.findTagInScreenCenter("img", driver)
        .map { img =>
          val src = img.getDomAttribute("src")
          logger.info(s"The nearest image to the center has src: $src and covers the center point.")
          ExtractorPayload(Seq(new URL(src)), Extension.JPEG)
        }
