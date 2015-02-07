name := "xtended-xml"
organization := "com.github.elbywan"
version := "0.1-SNAPSHOT"
scalaVersion := "2.11.5"

val scalatestVersion   = "2.2.1"

libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
)

pomIncludeRepository := { _ => false }
pomExtra :=
    <url>https://github.com/elbywan/Neold</url>
        <licenses>
            <license>
                <name>GNU Gpl v3</name>
                <url>http://www.gnu.org/licenses/gpl.txt</url>
                <distribution>repo</distribution>
            </license>
        </licenses>
        <scm>
            <url>git@github.com:elbywan/neold.git</url>
            <connection>scm:git:git@github.com:elbywan/neold.git</connection>
        </scm>
        <developers>
            <developer>
                <id>elbywan</id>
                <name>Julien Elbaz</name>
                <url>https://github.com/elbywan</url>
            </developer>
        </developers>

publishArtifact in Test := false
publishMavenStyle := true
publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
