package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumInstagramMp4ExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumInstagramMp4Extractor extracts video from instagram post"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumInstagramMp4Extractor(cookies)

      val result = extractor.extract(new URL("https://www.instagram.com/p/DFMPaf9usvd/"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.urls.size == 2)
      assert(payload.extension == Extension.DUAL_TRACK_MP4)
    else
      assert(true)

  test("SeleniumInstagramMp4Extractor extracts video from instagram post #2"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumInstagramMp4Extractor(cookies)

      val result = extractor.extract(new URL("https://www.instagram.com/p/DEpIuKsO5c-/"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.urls.size == 2)
      assert(payload.extension == Extension.DUAL_TRACK_MP4)
    else
      assert(true)

  test("SeleniumInstagramMp4Extractor extracts video from instagram post #3"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumInstagramMp4Extractor(cookies)

      val result = extractor.extract(new URL("https://www.instagram.com/p/DASzWjlMtvC/"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.urls.size == 2)
      assert(payload.extension == Extension.DUAL_TRACK_MP4)
    else
      assert(true)

  test("SeleniumInstagramMp4Extractor extracts reels from instagram"):
    val cookies = sys.env.get("INSTAGRAM_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumInstagramMp4Extractor(cookies)

      val result = extractor.extract(new URL("https://www.instagram.com/reels/DFPz6WntSH9/"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.urls.size == 2)
      assert(payload.extension == Extension.DUAL_TRACK_MP4)
    else
      assert(true)
