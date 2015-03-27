name := """play-sqlite-bitcoinprivacy"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "org.xerial" % "sqlite-jdbc" % "3.7.15-M1"
)

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)


fork in run := false
