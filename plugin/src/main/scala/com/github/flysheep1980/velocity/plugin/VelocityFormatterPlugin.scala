package com.github.flysheep1980.velocity.plugin

import sbt._
import Keys._
import scala.collection.mutable

object VelocityFormatterPlugin extends Plugin {

  val velocitySourceDirectory = SettingKey[File]("velocity-source-directory", "Directory containing velocity template files.")
  val velocitySources = TaskKey[Seq[File]]("velocity-sources")
  val velocityFormat = TaskKey[Unit]("velocity-format", "Run to format velocity template file.")
  val velocityFormatConfig = SettingKey[VelocityFormatterConfig]("velocity-formatter-config")

  val velocityFormatSettings = Seq(
    velocitySources <<= velocitySourceDirectory.map { dir =>
      (dir ** "*.vm").get
    },
    velocityFormat <<= velocityFormatTask
  )

  def velocityFormatTask = (velocitySourceDirectory, velocitySources, velocityFormatConfig, streams) map {
    (dir, src, conf, s) =>
      {
        val encodeCharset = conf.config.get(VelocityFormatterConfigKey.EncodeCharset).getOrElse("utf-8")
        val lineSeparator = conf.config.get(VelocityFormatterConfigKey.LineSeparator).getOrElse("\n")
        val indentString = conf.config.get(VelocityFormatterConfigKey.IndentString).getOrElse("\t")

        s.log.info("format %d velocity template files in [%s].".format(src.length, dir.get.map(_.getPath).mkString(",")))
        src.get foreach { f =>
          s.log.info("Formatting [%s].".format(f.getPath))
          val result = com.github.flysheep1980.velocity.Formatter.formatFile(f, encodeCharset, lineSeparator, indentString)
          if (result.indentLevel != 0) s.log.warn("Invalid format found in [%s].".format(f.getPath))
        }
      }
  }

  object VelocityFormatterConfigKey extends Enumeration {
    val EncodeCharset, LineSeparator, IndentString = Value
  }

  case class VelocityFormatterConfig(val config: mutable.Map[VelocityFormatterConfigKey.Value, String] = mutable.Map.empty) {
    def setConfig(key: VelocityFormatterConfigKey.Value, value: String): VelocityFormatterConfig = {
      config.put(key, value)
      this
    }
  }

}
