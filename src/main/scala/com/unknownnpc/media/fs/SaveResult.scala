package com.unknownnpc.media.fs

import java.nio.file.Path

sealed trait SaveResult:
  def path: Path

case class ImageSaveResult(override val path: Path) extends SaveResult

case class VideoSaveResult(override val path: Path, width: Int, height: Int, thumbnail: Path) extends SaveResult
