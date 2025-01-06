package com.unknownnpc.media

import com.unknownnpc.media.extractor.model.{Extension, ExtractorPayload}
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

import java.net.URL

class FileStorageImplTest extends AnyFunSuite with MockFactory:

  test("save should process with m3u8 urls and create video file"):
    val fileStorage = new FileStorageImpl

    val headUrl = new URL("https://video.twimg.com/amplify_video/1875106984979992576/pl/avc1/720x1280/LhsM6lNXx0Y1H-wi.m3u8")
    val tailUrl = new URL("https://video.twimg.com/amplify_video/1875106984979992576/pl/mp4a/128000/TGf4xtHSMilWrFP-.m3u8")
    val extractorPayload = ExtractorPayload(Seq(headUrl, tailUrl), Extension.M3U8)

    val result = fileStorage.save(extractorPayload)

    assert(result.isRight)
    assert(result.toOption.get.toString.toLowerCase.contains(".mp4"))
