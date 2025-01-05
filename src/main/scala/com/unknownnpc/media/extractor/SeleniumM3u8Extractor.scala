package com.unknownnpc.media.extractor

import com.typesafe.scalalogging.StrictLogging
import com.unknownnpc.media.extractor.model.{CustomCookie, Extension, ExtractorPayload, Result}
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.devtools.v131.network.Network
import org.openqa.selenium.devtools.v131.network.model.RequestWillBeSent

import java.net.URL
import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import scala.jdk.CollectionConverters.*

class SeleniumM3u8Extractor(val customCookies: Seq[CustomCookie]) extends Extractor with SeleniumWebDriverLike with StrictLogging:

  val preConfigureFn: ChromeDriver => Unit = driver =>
    val devTools = driver.getDevTools
    devTools.createSession()
    devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))

    devTools.addListener(Network.requestWillBeSent(), new java.util.function.Consumer[RequestWillBeSent] {
      override def accept(request: RequestWillBeSent): Unit = {
        val requestUrl = request.getRequest.getUrl
        if (requestUrl.endsWith(".m3u8")) {
          m3u8Urls.add(requestUrl)
          logger.info(s"Found m3u8 URL: $requestUrl")
        }
      }
    })
  val mainFn: ChromeDriver => Option[ExtractorPayload] = driver =>
    if m3u8Urls.isEmpty then
      logger.info("No .m3u8 links found.")
      None
    else
      Some(ExtractorPayload(m3u8Urls.asScala.map(url => new URL(url)).toSet, Extension.M3U8))
  private val m3u8Urls = new CopyOnWriteArrayList[String]()

  override def extract(url: URL): Result =
    openPage(url, customCookies, 60_000)(mainFn, preConfigureFn)
