name := """ci6226"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

javaOptions += "-DentityExpansionLimit=1500000"
javaOptions += "-Xms1g -Xmx4g -Xss4M -XX:+CMSClassUnloadingEnabled"

lazy val `ci6226` = project.in(file(".")).enablePlugins(PlayScala)

libraryDependencies += filters

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-analyzers-common" % "5.5.0",
  "org.apache.lucene" % "lucene-benchmark" % "5.5.0",
  "org.apache.lucene" % "lucene-queryparser" % "5.5.0"
)

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars.bower" % "bootstrap" % "4.0.0-alpha.2",
  "org.webjars.bower" % "font-awesome" % "4.5.0",
  "org.webjars.bower" % "awesome-bootstrap-checkbox" % "0.3.7",
  "org.webjars" % "jquery" % "2.2.0"
)