import Dependencies._

organization := "org.shl"
name := "scql"
version := "0.0.1"

scalaVersion := "2.12.8"

logLevel := Level.Debug

coverageEnabled := false

scalastyleConfig := baseDirectory.value / "project/scalastyle-config.xml"

(scalastyleConfig in Test) := baseDirectory.value / "project/scalastyle-test-config.xml"

libraryDependencies ++= {
    scalaCore ++
    scalactic_scalatest ++
    liquibase ++
    hsqlTest ++
    javamail ++
    flexmark ++
    antisamy ++
    datastax ++
    Nil
}