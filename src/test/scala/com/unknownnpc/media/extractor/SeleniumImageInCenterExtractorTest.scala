package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.{CustomCookie, Extension}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumImageInCenterExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumImageInCenterExtractor extracts image from twitter"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)

    val result = extractor.extract(new URL("https://x.com/m13tfz08k/status/1874840987992027161"))

    assert(result.isRight)
    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.urls.head.toString == "https://pbs.twimg.com/media/GgTF8MBXIAAyMPs?format=jpg&name=small")
    assert(payload.extension == Extension.JPEG)

  test("SeleniumImageInCenterExtractor extracts two images in parallel"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)
    val resultOne = extractor.extract(new URL("https://x.com/m13tfz08k/status/1874840987992027161"))
    val resultTwo = extractor.extract(new URL("https://x.com/frenchieclub247/status/1875542922982256887"))

    assert(resultOne.isRight)
    assert(resultTwo.isRight)

    val payloadOne = resultOne.getOrElse(throw new RuntimeException("Boom")).get
    assert(payloadOne.urls.head.toString == "https://pbs.twimg.com/media/GgTF8MBXIAAyMPs?format=jpg&name=small")
    assert(payloadOne.extension == Extension.JPEG)

    val payloadTwo = resultTwo.getOrElse(throw new RuntimeException("Boom")).get
    assert(payloadTwo.urls.head.toString == "https://pbs.twimg.com/media/GgdEWWHa0AA0si2?format=jpg&name=small")
    assert(payloadTwo.extension == Extension.JPEG)

  test("SeleniumImageInCenterExtractor extracts image from OF"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)
    val result = extractor.extract(new URL("https://onlyfans.com/anastasiadollofficial"))

    assert(result.isRight)

  test("SeleniumImageInCenterExtractor extracts image from OF shop"):
    val extractor = new SeleniumImageInCenterExtractor(Seq.empty)
    
    val result = extractor.extract(new URL("https://store.onlyfans.com/cdn/shop/files/ONLYFANSOct2-Square-3057.jpg?v=170"))

    assert(result.isRight)
    val payload = result.getOrElse(throw new RuntimeException("Boom")).get
    assert(payload.urls.head.toString == "https://store.onlyfans.com/cdn/shop/files/ONLYFANSOct2-Square-3057.jpg?v=170")
    assert(payload.extension == Extension.JPEG)

  test("SeleniumVideoInCenterExtractor extracts image from twitter when credentials are set"):
    val cookies = sys.env.get("TWITTER_CUSTOM_COOKIES").map(CustomCookie.from).getOrElse(Seq.empty)

    if cookies.nonEmpty then
      val extractor = new SeleniumImageInCenterExtractor(cookies)

      val result = extractor.extract(new URL("https://x.com/sexy__girlxxx/status/1834272702980346304?s=46"))

      val payload = result.getOrElse(throw new RuntimeException("Boom")).get
      assert(payload.urls.head.toString == "https://pbs.twimg.com/media/GXSlKInXoAAbMy_?format=jpg&name=900x900")
      assert(payload.extension == Extension.JPEG)
    else
      assert(true)
