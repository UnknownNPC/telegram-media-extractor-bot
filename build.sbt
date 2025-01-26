ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.2"
ThisBuild / concurrentRestrictions := Tags.limit(Tags.Test, 3) :: Nil
ThisBuild / resolvers += "BigBone lib repo" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"

lazy val root = (project in file("."))
  .settings(
    name := "telegram-media-extractor-bot",
  )
  .settings(assemblySettings *)

lazy val assemblySettings = Seq(
  assembly / assemblyJarName := "telegram-media-extractor-bot.jar",
  assembly / mainClass := Some("com.unknownnpc.media.AppMain"),
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "4.27.0",
  "ch.qos.logback" % "logback-classic" % "1.4.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "org.slf4j" % "jul-to-slf4j" % "2.0.16",
  "com.github.pengrad" % "java-telegram-bot-api" % "7.11.0",
  "org.twitter4j" % "twitter4j-core" % "2.1.8",
  "io.github.takke" % "jp.takke.twitter4j-v2" % "1.4.4",
  "org.apache.httpcomponents.client5" % "httpclient5" % "5.4",
  "social.bigbone" % "bigbone"  % "2.0.0-SNAPSHOT",
  "io.lemonlabs" %% "scala-uri" % "4.0.1",
  "io.github.json4s" %% "json4s-core" % "4.1.0-M9",
  "io.github.json4s" %% "json4s-jackson" % "4.1.0-M9",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalamock" %% "scalamock" % "6.1.1" % Test
)
