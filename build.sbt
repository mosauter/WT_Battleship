name := """Battleship"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
    "org.pac4j" % "play-pac4j" % "2.1.0",
    "org.pac4j" % "pac4j-http" % "1.8.3",
    "org.pac4j" % "pac4j-oidc" % "1.8.3",
    "com.typesafe.play" % "play-cache_2.11" % "2.4.0",
    "de.htwg.Battleship" % "Battleship" % "1.2-SNAPSHOT",
    javaJdbc,
    cache,
    javaWs
)


// resolvers := Seq(Resolver.mavenLocal)

resolvers ++= Seq(Resolver.mavenLocal)

resolvers += "PhaseX_Nexus" at "http://nexus-phasex.rhcloud.com/content/groups/public"
resolvers += Resolver.sonatypeRepo("snapshots")

routesGenerator := InjectedRoutesGenerator


libraryDependencies += "com.google.code.gson" % "gson" % "2.5"
// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.

fork in run := true
fork in run := true
