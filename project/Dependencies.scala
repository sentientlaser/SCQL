import sbt.Keys.ivyConfigurations
import sbt._

object Dependencies {

  val CompileOnly = config("compileonly")
  ivyConfigurations += CompileOnly.hide

  val sonarSupport = Seq(
    "com.qualinsight.plugins.sonarqube" % "qualinsight-plugins-sonarqube-smell-api"
  ).map(_ % "4.0.0" % CompileOnly)

  val scalaCore = Seq(
    "org.scala-lang" % "scala-reflect"
  ).map(_ % "2.12.7" % Compile)

  val javamail = Seq (
    "javax.mail" % "javax.mail-api"
  ).map(_  % "1.6.2" % Compile)

  val flexmark = Seq (
    "com.vladsch.flexmark" % "flexmark-all"
  ).map(_ % "0.50.4" % Compile )

  val antisamy = Seq (
    "org.owasp.antisamy" % "antisamy"
  ).map(_ % "1.5.8" % Compile )

  val cassandraunit = Seq (
    "org.cassandraunit" % "cassandra-unit"
  ).map(_ % "3.11.2.0" % Test)

  val datastax = Seq (
    "com.datastax.oss" % "java-driver-core",
    "com.datastax.oss" % "java-driver-query-builder"
  ).map(_  % "4.1.0" % Compile)

  val cassandraphantom = Seq (
    //  "com.outworkers"   %% "phantom-sbt",
    //  "com.outworkers"   %% "phantom-example",
    //  "com.outworkers"   %% "phantom-finagle",
    //  "com.outworkers"   %% "phantom-thrift",
    //  "com.outworkers"   %% "phantom-streams",
    "com.outworkers"   %% "phantom-jdk8",
    "com.outworkers"   %% "phantom-connectors",
    "com.outworkers"   %% "phantom-dsl"
  ).map(_ % "2.41.0" % Compile)

  val liquibase = Seq(
    "org.liquibase" % "liquibase-core", // exclude ("ch.qos.logback", "logback-classic")
  ).map(_ % "3.6.2" % Compile)

  val jackson = Seq(
      //"com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml",
      //"com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml",
      "com.fasterxml.jackson.module" %% "jackson-module-scala",
      "com.fasterxml.jackson.core" % "jackson-databind"
    ).map(_ % "2.9.7" % Compile )
  
  
  val swagger = Seq{
    "io.swagger" % "swagger-annotations" 
  }.map (_ % "2.0.0-rc2" % Compile)

  val scalactic_scalatest = {
    val version = "3.0.5"
    Seq(
      "org.scalactic" %% "scalactic" % version % Compile,
      "org.scalatest" %% "scalatest" % version % Test,
    )
  }

  val hsqlTest = Seq(
    "org.hsqldb" % "hsqldb"
  ).map(_ % "2.4.1" % Test)

}
