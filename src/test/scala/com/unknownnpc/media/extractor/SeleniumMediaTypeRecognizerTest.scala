
package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.MediaType.{DualTrackMp4Page, ImagePage, U3M8Page}
import com.unknownnpc.media.extractor.model.{CustomCookie, MediaType}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumMediaTypeRecognizerTest extends AnyFunSuite with Matchers:

  test("SeleniumMediaTypeRecognizer should recognize twitter page with m3u8 streams"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://x.com/SweetieFox1/status/1880631773824008463"))

      assert(result == U3M8Page)
    else
      assert(true)

  test("SeleniumMediaTypeRecognizer should recognize twitter page with m3u8 streams #2"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://x.com/_dianarider_/status/1883247402456600736?s=46"))

      assert(result == U3M8Page)
    else
      assert(true)

  test("SeleniumMediaTypeRecognizer should recognize twitter image in the screen center"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://x.com/_HaileyQueen_/status/1863595313660911801"))

      assert(result == ImagePage)
    else
      assert(true)

  test("SeleniumMediaTypeRecognizer should recognize instagram post with image in the screen center"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://www.instagram.com/p/DFQacrktmDA"))

      assert(result == ImagePage)
    else
      assert(true)

  test("SeleniumMediaTypeRecognizer should recognize instagram post with video in the screen center"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://www.instagram.com/p/DFQmxqBtgXT/"))

      assert(result == DualTrackMp4Page)
    else
      assert(true)

  test("SeleniumMediaTypeRecognizer should recognize instagram reels in the screen center"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val recognizer = new SeleniumMediaTypeRecognizer(cookies)

      val result = recognizer.getMediaType(new URL("https://www.instagram.com/reel/DErzvhoA6G4/"))

      assert(result == DualTrackMp4Page)
    else
      assert(true)
