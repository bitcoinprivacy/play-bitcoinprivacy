//import AssemblyKeys._

//assemblySettings

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

//import NativePackagerKeys._

name := """play-bitcoinprivacy"""

version := "2.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
//  jdbc,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  ws,
//  evolutions,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
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

resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

fork in run := false

// setting a maintainer which is used for all packaging types
maintainer := "Stefan and Jorge"

// exposing the play ports
//dockerExposedPorts in Docker := Seq(9000, 9443)

// run this with: docker run -p 9000:9000 play-2-3:1.0-SNAPSHOT

assemblyMergeStrategy in assembly := { 
case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate

    }
  case _ => MergeStrategy.first
  }

