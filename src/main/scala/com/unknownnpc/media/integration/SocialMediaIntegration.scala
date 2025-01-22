package com.unknownnpc.media.integration

import com.unknownnpc.media.extractor.model.Extension
import com.unknownnpc.media.fs.SaveResult

import java.nio.file.Path

trait SocialMediaIntegration:

  def name: String

  def send(filePath: SaveResult): IntegrationResult
