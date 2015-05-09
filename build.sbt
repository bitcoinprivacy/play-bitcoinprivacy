
name := """play-bitcoinprivacy"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  jdbc,
  anorm,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "org.xerial" % "sqlite-jdbc" % "3.7.15-M1",
  "org.bitcoinj" % "bitcoinj-core" % "0.12",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4"
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
