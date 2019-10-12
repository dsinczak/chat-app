
val akkaVersion = "2.5.25"
val akkaHttpVersion = "10.1.10"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test
val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.14.2" % Test
val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
val akkaHttp = "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

// wartremoverErrors ++= Warts.unsafe

val root = (project in file("."))
  .settings(
    name := """chat-app""",
    version := "1.0",
    scalaVersion := "2.13.1"
  ).settings(
  libraryDependencies ++= Seq(
    akka,
    akkaHttp,
    akkaStream,
    sprayJson,
    // Test
    scalaTest,
    scalaCheck,
    akkaTestKit
  ))
  //.settings(scalacOptions += "-Ypartial-unification") // for cats higher-kinded types

