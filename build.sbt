import NativePackagerKeys._

name := """play-bitcoinprivacy"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  jdbc,
  anorm,
  "mysql" % "mysql-connector-java" % "5.1.18",
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

// setting a maintainer which is used for all packaging types
maintainer := "Stefan and Jorge"

// exposing the play ports
dockerExposedPorts in Docker := Seq(9000, 9443)

// run this with: docker run -p 9000:9000 play-2-3:1.0-SNAPSHOT
