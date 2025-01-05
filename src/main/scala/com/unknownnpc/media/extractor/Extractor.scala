package com.unknownnpc.media.extractor

import com.unknownnpc.media.extractor.model.Result

import java.net.URL

trait Extractor:

  def extract(url: URL): Result
