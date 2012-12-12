package com.github.flysheep1980.velocity.plugin

import sbt._
import Keys._

object VelocityFormatterPlugin extends Plugin {

  val velocitySourceDirectory = SettingKey[File]("velocity-source-directory")
  val velocitySources = TaskKey[Seq[File]]("velocity-sources")
  val velocityFormat = TaskKey[Unit]("velocity-format", "Run to format velocity template file.")

  val velocityFormatSettings = Seq(
    velocitySources <<= velocitySourceDirectory.map { dir =>
      (dir ** "*.vm").get
    },
    velocityFormat <<= velocityFormatTask
  )

  def velocityFormatTask = (velocitySourceDirectory, velocitySources, streams) map {
    (dir, src, s) =>
      {
        s.log.info("format %d velocity template files in [%s]".format(src.length, dir.get.map(_.getPath).mkString(",")))
        src.get foreach { f =>
          s.log.info("Formatting [%s]".format(f.getPath))
          com.github.flysheep1980.velocity.Formatter.formatFile(f)
        }
      }
  }

}
