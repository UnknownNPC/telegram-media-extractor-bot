package com.unknownnpc.media.integration

import com.unknownnpc.media.fs.SaveResult

trait SocialMediaIntegration:

  def name: String

  def send(filePath: SaveResult): IntegrationResult
