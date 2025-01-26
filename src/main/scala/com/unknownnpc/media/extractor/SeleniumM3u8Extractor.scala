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

class SeleniumM3u8Extractor(val customCookies: Seq[CustomCookie])
  extends Extractor[Option[ExtractorPayload]] with SeleniumWebDriverLike with StrictLogging:

  val preConfigureFn: ChromeDriver => CopyOnWriteArrayList[String] = driver =>
    SeleniumUtil.runUrlsListenerScanner(_.endsWith("m3u8"))(driver)

  val mainFn: (ChromeDriver, CopyOnWriteArrayList[String]) => Option[ExtractorPayload] = (driver, m3u8Urls) =>
    if m3u8Urls.isEmpty then
      logger.info("No .m3u8 links found.")
      None
    else
      val m3u8UrlsScala = m3u8Urls.asScala.toSeq
      logger.info(s"Found the following playlist urls:\n${m3u8UrlsScala.mkString("\n")}")
      if m3u8UrlsScala.size >= 2 then
        val m3u8FirstTwo = m3u8UrlsScala.take(2)
        logger.info(s"Taking the first two urls:\n${m3u8FirstTwo.mkString("\n")}")
        Some(ExtractorPayload(m3u8FirstTwo.map(url => new URL(url)), Extension.M3U8))
      else
        logger.error(s"Unable to get at least two stream playlists")
        None

  override def extract(url: URL): Result[Option[ExtractorPayload]] =
    openPage(url, customCookies)(mainFn, preConfigureFn)
