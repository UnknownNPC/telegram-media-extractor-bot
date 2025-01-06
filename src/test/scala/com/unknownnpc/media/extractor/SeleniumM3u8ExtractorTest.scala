package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.Extension
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumM3u8ExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumM3u8Extractor extracts short video from twitter"):
    val extractor = new SeleniumM3u8Extractor(Seq.empty)

    val result = extractor.extract(new URL("https://x.com/contextdogs/status/1875107037006463490"))

    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.urls.size == 2)
    assert(payload.urls.contains(new URL("https://video.twimg.com/amplify_video/1875106984979992576/pl/mp4a/128000/TGf4xtHSMilWrFP-.m3u8")))
    assert(payload.urls.contains(new URL("https://video.twimg.com/amplify_video/1875106984979992576/pl/avc1/720x1280/LhsM6lNXx0Y1H-wi.m3u8")))
    assert(payload.extension == Extension.M3U8)

  test("SeleniumM3u8Extractor extracts short video from twitter #2"):
    val extractor = new SeleniumM3u8Extractor(Seq.empty)

    val result = extractor.extract(new URL("https://x.com/contextdogs/status/1410694888409255936"))

    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.urls.size == 2)
    assert(payload.urls.contains(new URL("https://video.twimg.com/ext_tw_video/1410694805018009603/pu/pl/640x640/UgGuJpFHSwWhREFw.m3u8")))
    assert(payload.urls.contains(new URL("https://video.twimg.com/ext_tw_video/1410694805018009603/pu/pl/mp4a/128000/SHjHWvWmjDHlRRqi.m3u8")))
    assert(payload.extension == Extension.M3U8)
