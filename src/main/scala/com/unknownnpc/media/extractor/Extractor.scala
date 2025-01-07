package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.Result

import java.net.URL

trait Extractor[T]:

  def extract(url: URL): Result[T]
