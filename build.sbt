ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.2"
ThisBuild / concurrentRestrictions := Tags.limit(Tags.Test, 3) :: Nil

lazy val root = (project in file("."))
  .settings(
    name := "telegram-media-extractor-bot",
  )

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "4.27.0",
  "ch.qos.logback" % "logback-classic" % "1.4.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "org.slf4j" % "jul-to-slf4j" % "2.0.16",
  "com.github.pengrad" % "java-telegram-bot-api" % "7.11.0",
  "io.github.redouane59.twitter" % "twittered" % "2.23",
  "org.apache.httpcomponents.client5" % "httpclient5" % "5.4",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.scalamock" %% "scalamock" % "6.1.1" % Test
)
