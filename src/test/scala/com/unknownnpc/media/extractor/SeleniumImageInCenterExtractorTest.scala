package com.unknownnpc.media.extractor

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumImageInCenterExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumImageInCenterExtractor extracts image from twitter"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)
    val result = extractor.extract(new URL("https://x.com/m13tfz08k/status/1874840987992027161"))

    assert(result.isRight)
    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.url.toString == "https://pbs.twimg.com/media/GgTF8MBXIAAyMPs?format=jpg&name=small")
    assert(payload.extension == Extension.JPEG)


  test("SeleniumImageInCenterExtractor extracts image from OF"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)
    val result = extractor.extract(new URL("https://onlyfans.com/anastasiadollofficial"))

    assert(result.isRight)

  test("SeleniumImageInCenterExtractor extracts image from OF shop"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)
    val result = extractor.extract(new URL("https://store.onlyfans.com/cdn/shop/files/ONLYFANSOct2-Square-3057.jpg?v=170"))

    assert(result.isRight)
    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.url.toString == "https://store.onlyfans.com/cdn/shop/files/ONLYFANSOct2-Square-3057.jpg?v=170")
    assert(payload.extension == Extension.JPEG)
