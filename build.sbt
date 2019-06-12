import Dependencies._

organization := "org.shl"
name := "BattleChatter"
version := "0.0.1"

scalaVersion := "2.12.8"

coverageEnabled := true

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
    Nil
}