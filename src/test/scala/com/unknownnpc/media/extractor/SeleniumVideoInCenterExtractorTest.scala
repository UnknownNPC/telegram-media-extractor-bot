package com.unknownnpc.media.extractor

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumVideoInCenterExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumVideoInCenterExtractor extracts short video from twitter"):
    val extractor = new SeleniumVideoInCenterExtractor(Seq.empty)
    val result = extractor.extract(new URL("https://x.com/alexnivak/status/1871251416050020525"))

    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.url.toString == "https://video.twimg.com/tweet_video/GfgFPX9W0AAcLcF.mp4")
    assert(payload.extension == Extension.MP4)

  test("SeleniumVideoInCenterExtractor extracts short video from twitter when credentials are set"):
    val cookies = sys.env.get("TEST_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumVideoInCenterExtractor(cookies)

      val result = extractor.extract(new URL("https://x.com/traumaxdesire/status/1874582816945758300"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.url.toString == "https://video.twimg.com/tweet_video/GgPbHDsWQAAPf5n.mp4")
      assert(payload.extension == Extension.MP4)
    else
      assert(true)
