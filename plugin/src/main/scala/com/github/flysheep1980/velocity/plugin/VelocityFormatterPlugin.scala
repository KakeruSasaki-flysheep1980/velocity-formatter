package com.github.flysheep1980.velocity.plugin

import sbt._
import Keys._

object VelocityFormatterPlugin extends Plugin {

  val velocitySourceDirectory = SettingKey[File]("velocity-source-directory")
  val velocitySources = TaskKey[Seq[File]]("velocity-sources")
  val velocityFormat = TaskKey[Unit]("velocity-format", "Run to format velocity template file.")

  val velocityFormatSettings = Seq(
    velocitySourceDirectory <<= resourceDirectory(_ / "vm"),
    velocitySources <<= velocitySourceDirectory.map { dir =>
      (dir ** "*.vm").get
    },
    velocityFormat <<= velocityFormatTask
  )

  def velocityFormatTask = (velocitySources, streams) map {
    (src, s) =>
      {
        s.log.info("velocity format... src is [%s]".format(src))
        src.get foreach { f =>
          s.log.info("velocity format... file is [%s]".format(f.getPath))
          com.github.flysheep1980.velocity.Formatter.formatFile(f)
        }
      }
  }

}
