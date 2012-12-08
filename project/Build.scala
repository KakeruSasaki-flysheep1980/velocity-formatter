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
    version := appVersion
  ).settings(scalariformSettings: _*).settings(myScalariformSettings)

}
