name := """test_scala"""
organization := "test"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test
libraryDependencies ++= Seq(
  jdbc,
  guice,
  evolutions,
  "org.playframework.anorm" %% "anorm" % "2.7.0",
  "mysql" % "mysql-connector-java" % "8.0.28",
  "com.typesafe.play" %% "play-jdbc" % "2.8.19",
  "com.typesafe.play" %% "play-jdbc-api" % "2.8.19"
)

dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.2.0"

enablePlugins(DockerPlugin)
