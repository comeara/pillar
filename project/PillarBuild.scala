import _root_.sbtassembly.Plugin.AssemblyKeys._
import java.util.NoSuchElementException
import sbt._
import Keys._
import sbtassembly.Plugin.{MergeStrategy, PathList}
import xerial.sbt.Sonatype

object PillarBuild extends Build {
  val assemblyTestSetting = test in assembly := {}
  val assemblyMergeStrategySetting = mergeStrategy in assembly <<= (mergeStrategy in assembly) {
    (old) => {
      case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
      case x => old(x)
    }
  }

  val dependencies = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0",
    "com.typesafe" % "config" % "1.0.1",
    "org.clapper" %% "argot" % "1.0.3",
    "org.mockito" % "mockito-core" % "1.9.5" % "test",
    "org.scalatest" %% "scalatest" % "2.2.0" % "test"
  )

  val rhPackage = TaskKey[File]("rh-package", "Packages the application for Red Hat Package Manager")
  val rhPackageTask = rhPackage <<= (sourceDirectory, target, assembly, version) map {
    (sourceDirectory: File, targetDirectory: File, archive: File, versionId: String) =>
      val rootPath = new File(targetDirectory, "staged-package")
      val subdirectories = Map(
        "bin" -> new File(rootPath, "bin"),
        "conf" -> new File(rootPath, "conf"),
        "lib" -> new File(rootPath, "lib")
      )
      subdirectories.foreach {
        case (_, subdirectory) => IO.createDirectory(subdirectory)
      }
      IO.copyFile(archive, new File(subdirectories("lib"), "pillar.jar"))
      val bashDirectory = new File(sourceDirectory, "main/bash")
      bashDirectory.list.foreach {
        script =>
          val destination = new File(subdirectories("bin"), script)
          IO.copyFile(new File(bashDirectory, script), destination)
          destination.setExecutable(true, false)
      }
      val resourcesDirectory = new File(sourceDirectory, "main/resources")
      resourcesDirectory.list.foreach {
        resource =>
          IO.copyFile(new File(resourcesDirectory, resource), new File(subdirectories("conf"), resource))
      }
      val iterationId = try { sys.env("GO_PIPELINE_COUNTER") } catch { case e: NoSuchElementException => "DEV" }
      "fpm -f -s dir -t rpm --package %s -n pillar --version %s --iteration %s -a all --prefix /opt/pillar -C %s/staged-package/ .".format(targetDirectory.getPath, versionId, iterationId, targetDirectory.getPath).!

      val pkg = file("%s/pillar-%s-%s.noarch.rpm".format(targetDirectory.getPath, versionId, iterationId))
      if(!pkg.exists()) throw new RuntimeException("Packaging failed. Check logs for fpm output.")
      pkg
  }

  lazy val root = Project(
    id = "pillar",
    base = file("."),
    settings = Project.defaultSettings ++ sbtassembly.Plugin.assemblySettings ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++ Sonatype.sonatypeSettings
  ).settings(
    assemblyMergeStrategySetting,
    assemblyTestSetting,
    libraryDependencies := dependencies,
    name := "pillar",
    organization := "com.chrisomeara",
    version := "2.1.0",
    homepage := Some(url("https://github.com/comeara/pillar")),
    licenses := Seq("MIT license" -> url("http://www.opensource.org/licenses/mit-license.php")),
    scalaVersion := "2.10.6",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    rhPackageTask
  ).settings(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra := (
      <scm>
        <url>git@github.com:comeara/pillar.git</url>
        <connection>scm:git:git@github.com:comeara/pillar.git</connection>
      </scm>
      <developers>
        <developer>
          <id>comeara</id>
          <name>Chris O'Meara</name>
          <url>https://github.com/comeara</url>
        </developer>
      </developers>
    )
  )
}
