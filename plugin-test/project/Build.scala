import sbt._
import Keys._

object ApplicationBuild extends Build {

  lazy val myVelocityPluginSettings = com.github.flysheep1980.velocity.plugin.VelocityFormatterPlugin.velocityFormatSettings ++ Seq(
    resourceDirectory <<= baseDirectory(_ / "resources")
  )

  lazy val pluginTest = Project("plugin-test", base = file("."), settings = Defaults.defaultSettings ++ myVelocityPluginSettings ++ Seq(
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = false)
    }
  ))

}