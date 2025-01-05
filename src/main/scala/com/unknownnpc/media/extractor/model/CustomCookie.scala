package com.unknownnpc.media.extractor.model

case class CustomCookie(key: String, value: String)

object CustomCookie:
  def from(str: String): Seq[CustomCookie] =
    val cookieStrPairs = str.split(";")
    cookieStrPairs.map(cookiePair => cookiePair.split(":") match
      case Array(key, value) => CustomCookie(key, value)
      case _ => throw new RuntimeException(s"Invalid key:par format: $cookiePair")
    )
