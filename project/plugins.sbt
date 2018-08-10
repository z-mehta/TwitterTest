// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

resolvers += Resolver.sonatypeRepo("public")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.3.0")