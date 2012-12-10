import sbt._
import Keys._
import com.typesafe.sbt.SbtScalariform._
import scalariform.formatter.preferences._

object ApplicationBuild extends Build {

  val appOrganization	= "com.github.flysheep1980"
  val appName         = "velocity-formatter"
  val appVersion      = "0.1-SNAPSHOT"

  lazy val myScalariformSettings = ScalariformKeys.preferences := FormattingPreferences()
    .setPreference(IndentWithTabs, false)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(PreserveDanglingCloseParenthesis, true)

  lazy val root = Project("root", base = file(".")).aggregate(core, plugin)

  lazy val core = Project("core", base = file("core")).settings(
    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.12.4-SNAPSHOT" % "test"
    )
  ).settings(scalariformSettings: _*).settings(myScalariformSettings)

  lazy val plugin = Project("plugin", base = file("plugin")).settings(
    sbtPlugin := true,
    name := appName + "-plugin",
    organization := appOrganization,
    version := appVersion
  ).settings(scalariformSettings: _*).settings(myScalariformSettings).dependsOn(core)

}
