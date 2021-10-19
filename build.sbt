name := "GeoService"
 
version := "1.0" 
      
lazy val `geoservice` = (project in file(".")).enablePlugins(PlayScala)

      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.5"

libraryDependencies ++= Seq( evolutions , ehcache , ws , specs2 % Test , guice )

libraryDependencies ++= Seq(
  //  "com.typesafe.slick" %% "slick" % "3.3.3",
  //  "org.slf4j" % "slf4j-nop" % "1.7.32",
  //  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
  )

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.24"

      