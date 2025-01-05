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
    payload.urls.foreach(url => assert(url.toString.endsWith(".m3u8")))
    assert(payload.urls.size == 2)
    assert(payload.extension == Extension.M3U8)
