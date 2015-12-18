name := """Battleship"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "ws.securesocial" %% "securesocial" % "3.0-M4"
)

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.google.code.gson" % "gson" % "2.5"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

fork in run := true
fork in run := true
fork in run := true


fork in run := true