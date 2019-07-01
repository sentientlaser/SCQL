scalaVersion := "2.12.8"
resolvers += Resolver.sonatypeRepo("releases")


addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.jetbrains" %% "sbt-idea-plugin" % "2.1.3")
addSbtPlugin("com.typesafe.sbteclipse" %% "sbteclipse-plugin" % "5.2.4")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
