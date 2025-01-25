
package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.MediaType.U3M8Page
import com.unknownnpc.media.extractor.model.{CustomCookie, MediaType}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumMediaTypeRecognizerTest extends AnyFunSuite with Matchers:

  test("SeleniumMediaTypeRecognizer should recognize page with m3u8 streams"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://x.com/SweetieFox1/status/1880631773824008463"))

      assert(result == U3M8Page)
    else
      assert(true)

  test("SeleniumMediaTypeRecognizer should recognize page with m3u8 streams #2"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://x.com/_dianarider_/status/1883247402456600736?s=46"))

      assert(result == U3M8Page)
    else
      assert(true)
