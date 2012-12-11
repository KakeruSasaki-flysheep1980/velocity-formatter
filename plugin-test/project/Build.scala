import sbt._
import Keys._

object ApplicationBuild extends Build {

  lazy val pluginTest = Project("plugin-test", base = file(".")).settings(
    resourceDirectory <<= baseDirectory(_ / "resources"),
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = false)
    }
  ).settings(com.github.flysheep1980.velocity.plugin.VelocityFormatterPlugin.velocityFormatSettings: _*)
}