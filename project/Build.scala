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

  lazy val root = Project(appName, base = file(".")).settings(
    organization := appOrganization,
    version := appVersion,

    resolvers ++= Seq(
      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "releases" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.12.4-SNAPSHOT" % "test"
    )
  ).settings(scalariformSettings: _*).settings(myScalariformSettings)

}
