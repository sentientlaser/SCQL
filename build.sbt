import Dependencies._

organization := "org.shl"
name := "scql"
version := "0.0.1"

scalaVersion := "2.12.8"

logLevel := Level.Info

coverageEnabled := false

autoCompilerPlugins := true

scalastyleConfig := baseDirectory.value / "project/scalastyle-config.xml"

(scalastyleConfig in Test) := baseDirectory.value / "project/scalastyle-test-config.xml"

libraryDependencies ++= {
    scalaCore ++
    scalactic_scalatest ++
    datastax ++
    Seq (
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
    )
}

excludeDependencies += "ch.qos.logback" % "logback-classic"
excludeDependencies += "log4j" % "log4j"
excludeDependencies += "commons-logging" % "commons-logging"


mainClass in (Compile, run) := Some("org.shl.scql.Example")


//addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
//scalacOptions ++= Seq("-Ymacro-debug-lite")
