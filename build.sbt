name                     := "spark-x"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.github.whitechno"
ThisBuild / scalaVersion := library.versions.scala212
//ThisBuild / sparkVersion := library.versions.spark30

lazy val `simple-project` = project
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark30provided,
    libraryDependencies ++= Seq(
      library.typesafeConfig % "provided",
      library.scalatest      % Test
    ),
    crossScalaVersions         := library.supportedScalaVersions,
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
    // to put "provided" Spark dependencies back to run classpath:
    run in Compile := Defaults
      .runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
      .evaluated,
    runMain in Compile := Defaults
      .runMainTask(fullClasspath in Compile, runner in (Compile, run))
      .evaluated
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
    val spark24        = "2.4.6" // Jun 05, 2020
    val spark30        = "3.0.0" // Jun 18, 2020
    val scalatest      = "3.2.0"
    val typesafeConfig = "1.4.0"
  }

  val supportedScalaVersions = List(versions.scala211, versions.scala212)

  val sparkLibs       = Seq("core", "sql")
  val spark30         = sparkLibs.map { lib => "org.apache.spark" %% s"spark-${lib}" % versions.spark30 }
  val spark30provided = spark30.map { _ % "provided" }

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
