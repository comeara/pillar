lazy val pillarVersion = "3.0.SNAPSHOT"

organization := "com.chrisomeara"
name := "pillar"
version := pillarVersion
scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")
homepage := Some(url("https://github.com/comeara/pillar"))
licenses := Seq("MIT license" -> url("http://www.opensource.org/licenses/mit-license.php"))
libraryDependencies ++= Seq(
  "com.chrisomeara" %% "pillar-core" % pillarVersion,
  "com.github.scopt" %% "scopt" % "3.5.0",
  "com.typesafe" % "config" % "1.0.1",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.22"
)
publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
publishArtifact in Test := false
pomIncludeRepository := { _ => false }
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
    </developers>)

test in assembly := {}
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
