package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.CustomCookie
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class ExtractorServiceTest extends AnyFunSuite with Matchers:
  test("ExtractorService should extract video with video in comments"):
    val service = ExtractorService(Seq.empty)
    val result = service.getMediaUrl("https://x.com/interesting_aIl/status/1876109490363212023")

    assert(result.get.urls.contains(new URL("https://video.twimg.com/ext_tw_video/1876109442489470976/pu/pl/mp4a/128000/KLMhVpFvZOrDd0on.m3u8")))
    assert(result.get.urls.contains(new URL("https://video.twimg.com/ext_tw_video/1876109442489470976/pu/pl/avc1/576x1024/jirw8DOtolrKhYrz.m3u8")))

  test("ExtractorService should extract image with gif in comments"):
    val service = ExtractorService(Seq.empty)
    val result = service.getMediaUrl("https://x.com/MangaContexts/status/1876333918934179965")

    assert(result.get.urls.head.toString == "https://pbs.twimg.com/media/GgoTwVwXwAEuqMf?format=jpg&name=small")

  test("ExtractorService should extract image with one additional image below"):
    val service = ExtractorService(Seq.empty)
    val result = service.getMediaUrl("https://x.com/SandyofCthulhu/status/1876328246721319394")

    assert(result.get.urls.head.toString == "https://pbs.twimg.com/media/GgoOl41bEAA1wi3?format=png&name=small")

  ignore("ExtractorService should extract image with one additional image above"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val service = ExtractorService(Seq.empty)

      val result = service.getMediaUrl("https://x.com/Goddesscleo_1/status/1876329516840137009")

      assert(result.get.urls.head.toString == "")
    else
      assert(true)
