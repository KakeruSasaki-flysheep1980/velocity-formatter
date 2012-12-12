import sbt._
import Keys._
import com.typesafe.sbtscalariform.ScalariformPlugin._
import scalariform.formatter.preferences._

object ApplicationBuild extends Build {

  val appOrganization	= "com.github.flysheep1980"
  val appName         = "velocity-formatter"
  val pluginName      = appName + "-plugin"
  val appVersion      = "0.1-SNAPSHOT"

  lazy val standardSettings = Defaults.defaultSettings ++ myScalariformSettings

  lazy val myScalariformSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := FormattingPreferences()
      .setPreference(IndentWithTabs, false)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(PreserveDanglingCloseParenthesis, true)
  )

  lazy val root = Project("root", base = file(".")).aggregate(core, plugin)

  lazy val plugin = Project("plugin", base = file("plugin"), settings = standardSettings ++ Seq(
    sbtPlugin := true,
    name := pluginName,
    organization := appOrganization,
    version := appVersion
  )).dependsOn(core)

  lazy val core = Project("core", base = file("core"), settings = standardSettings ++ Seq(
    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.12.4-SNAPSHOT" % "test"
    ),
    name := appName,
    organization := appOrganization,
    version := appVersion
  ))

}
