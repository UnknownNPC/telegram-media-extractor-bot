package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumMp4VideoExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumMp4VideoExtractorTest extracts short video from twitter"):
    val extractor = new SeleniumMp4VideoExtractor(Seq.empty)

    val result = extractor.extract(new URL("https://x.com/alexnivak/status/1871251416050020525"))

    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.urls.head.toString == "https://video.twimg.com/tweet_video/GfgFPX9W0AAcLcF.mp4")
    assert(payload.extension == Extension.MP4)

  test("SeleniumMp4VideoExtractorTest extracts short video from twitter when credentials are set"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumMp4VideoExtractor(cookies)

      val result = extractor.extract(new URL("https://x.com/traumaxdesire/status/1874582816945758300"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.urls.head.toString == "https://video.twimg.com/tweet_video/GgPbHDsWQAAPf5n.mp4")
      assert(payload.extension == Extension.MP4)
    else
      assert(true)

  test("SeleniumMp4VideoExtractorTest return nothing when video is not in the viewport"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumMp4VideoExtractor(cookies)

      val result = extractor.extract(new URL("https://x.com/fuckt43338/status/1871243096740249974?s=46"))

      assert(result.isRight)
      assert(result.getOrElse(throw new RuntimeException("Boom")).isEmpty)
    else
      assert(true)
