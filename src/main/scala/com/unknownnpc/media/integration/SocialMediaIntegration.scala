package com.unknownnpc.media.integration

import com.unknownnpc.media.extractor.model.Extension

import java.nio.file.Path

trait SocialMediaIntegration:

  def name: String

  def send(filePath: Path, extension: Extension): IntegrationResult
