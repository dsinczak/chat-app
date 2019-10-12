val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % "test"
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.2" % "test"
val akka = "com.typesafe.akka" %% "akka-actor" % "2.5.25"
val akkaHttp = "com.typesafe.akka" %% "akka-http"   % "10.1.10"
val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.25"


// wartremoverErrors ++= Warts.unsafe

val root = (project in file("."))
  .settings(
    name := """chat-app""",
    version := "1.0",
    scalaVersion := "2.13.1"
  ).settings(
  libraryDependencies ++= Seq(
    scalaTest,
    scalaCheck,
    akka,
    akkaHttp,
    akkaStream
  ))
  //.settings(scalacOptions += "-Ypartial-unification") // for cats higher-kinded types

