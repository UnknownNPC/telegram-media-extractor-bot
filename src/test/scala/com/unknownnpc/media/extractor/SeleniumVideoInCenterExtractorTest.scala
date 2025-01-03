package com.unknownnpc.media.extractor

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.net.URL

class SeleniumVideoInCenterExtractorTest extends AnyFunSuite with Matchers:

  test("SeleniumVideoInCenterExtractor extracts short video from twitter"):
    val extractor = new SeleniumVideoInCenterExtractor
    val result = extractor.extract(new URL("https://x.com/alexnivak/status/1871251416050020525"))

    assert(result.isRight)
    val imgUrl = result.getOrElse(new RuntimeException("Boom")).toString
    assert(imgUrl == "https://video.twimg.com/tweet_video/GfgFPX9W0AAcLcF.mp4")
