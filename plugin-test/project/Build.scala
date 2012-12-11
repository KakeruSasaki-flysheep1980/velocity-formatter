import sbt._
import Keys._

object ApplicationBuild extends Build {

  import com.github.flysheep1980.velocity.plugin.VelocityFormatterPlugin

  lazy val pluginTest = Project("plugin-test", base = file("."), settings = Defaults.defaultSettings ++ VelocityFormatterPlugin.velocityFormatSettings ++ Seq(
    VelocityFormatterPlugin.velocitySourceDirectory <<= baseDirectory(_ / "resources" / "vm")
  ))

}