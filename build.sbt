name                     := "subutai-template"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.github.whitechno"
ThisBuild / scalaVersion := library.versions.scala212

lazy val `template-project` = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      library.typesafeConfig % "provided",
      library.scalatest      % Test
    ),
    crossScalaVersions         := library.supportedScalaVersions,
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

//
// versions and settings
//

lazy val library = new {

  val versions = new {
    val scala210       = "2.10.7"
    val scala211       = "2.11.12"
    val scala212       = "2.12.11"
    val scala213       = "2.13.3"
    val scalatest      = "3.2.0"
    val typesafeConfig = "1.4.0"
  }

  val supportedScalaVersions = List(versions.scala211, versions.scala212, versions.scala213)

  val scalatest      = "org.scalatest" %% "scalatest" % versions.scalatest
  val typesafeConfig = "com.typesafe"   % "config"    % versions.typesafeConfig

}

lazy val commonSettings = List(
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature" // [warn] there were 21 feature warnings; re-run with -feature for details
  ),
  test in assembly := {}
)
