name := "xtended-xml"
organization := "org.xml.xtended"
version := "0.1-SNAPSHOT"
scalaVersion := "2.11.5"

val scalatestVersion   = "2.2.1"

libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
)
    