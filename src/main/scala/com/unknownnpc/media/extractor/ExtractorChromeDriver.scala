package com.unknownnpc.media.extractor

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

import scala.util.Using

private[extractor] object ExtractorChromeDriver:

  val ScreenHeight = 1080L
  val ScreenWidth = 1920L
  private val options = new ChromeOptions() {
    addArguments("--no-sandbox")
    addArguments("--headless")
    addArguments("--enable-javascript")
    addArguments("--disable-gpu")
    addArguments(s"--window-size=$ScreenWidth,$ScreenHeight")
  }

  def getInstance(): ChromeDriver = new ChromeDriver(options)

  given Using.Releasable[ChromeDriver] with
    def release(driver: ChromeDriver): Unit = driver.quit()
