// Comment to get more information during initialization
logLevel := Level.Debug

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")
addSbtPlugin("com.typesafe.play" % "sbt-fork-run-plugin" % "2.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
