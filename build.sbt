import PlayKeys._
import scalariform.formatter.preferences._

name := """Battleship"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

scalariformSettings

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "ws.securesocial" %% "securesocial" % "3.0-M4",
  "ws.securesocial" %% "securesocial" % version.value, javaCore
)

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.google.code.gson" % "gson" % "2.5"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

fork in run := true
fork in run := true
fork in run := true


fork in run := true



javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint:-options")
scalacOptions := Seq("-encoding", "UTF-8", "-Xlint", "-deprecation", "-unchecked", "-feature")




fork in run := true