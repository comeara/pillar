import sbt._
import Keys._

object PillarBuild extends Build {
  val dependencies = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % "1.0.0",
    "com.typesafe" % "config" % "1.0.1",
    "org.clapper" %% "argot" % "1.0.1",
    "org.mockito" % "mockito-core" % "1.9.5" % "test",
    "org.scala-lang" % "scala-library" % "2.10.1",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
  )

  lazy val root = Project(
    id = "pillar",
    base = file("."),
    settings = Project.defaultSettings
  ).settings(
    libraryDependencies := dependencies,
    name := "pillar",
    organization := "streamsend",
    scalaVersion := "2.10.1",
    version := "1.0.0"
  )
}
