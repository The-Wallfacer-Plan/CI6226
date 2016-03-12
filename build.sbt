name := """ci6226"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

javaOptions += "-DentityExpansionLimit=1500000"

lazy val `ci6226` = project.in(file(".")).enablePlugins(PlayScala)

libraryDependencies += filters

libraryDependencies += "org.apache.lucene" % "lucene-analyzers-common" % "5.4.1"
libraryDependencies += "org.apache.lucene" % "lucene-queryparser" % "5.4.1"
libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "org.webjars" % "jquery" % "2.2.0"
)