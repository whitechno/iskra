ThisBuild / version      := "0.1.2" //-SNAPSHOT
ThisBuild / organization := "com.github.whitechno.iskra"
ThisBuild / scalaVersion := library.versions.scala213
// Spark 4 works only with Scala 2.13+ and Java 11+
// This project is set with Java 17
//ThisBuild / crossScalaVersions := library.supportedScalaVersions

/**
 * Assembly JAR for spark-submit: Spark libraries are "provided", but typesafeConfig
 * has to be included.
 */
lazy val `simple-spark-submit` = project
  .settings(
    commonSettings,
    assemblySettings,
    libraryDependencies ++= library.spark4provided,
    libraryDependencies += library.typesafeConfig,
    // In order to runMain in SBT (as opposed to using 'spark-submit')
    // sbt> simple-spark-submit / runMain iskra.SimpleApp local[4]
    // we need to put "provided" Spark dependencies back to run classpath:
    runWithProvidedSettings
    // classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary
  )

/**
 * Assembly JAR for Databricks: derived from `simple-spark-submit`, but with
 * typesafeConfig excluded (it is provided in Databricks Spark env).
 */
lazy val `simple-spark-databricks` = project
  .dependsOn(`simple-spark-submit`)
  .settings(
    commonSettings,
    assemblySettings
  )
  .settings(
    excludeDependencies += ExclusionRule(
      organization = library.typesafeConfig.organization,
      name         = library.typesafeConfig.name
    )
  )

/**
 * Assembly JAR with some `simple-spark-submit` dependencies excluded. The generated
 * assembly JAR is exactly the same as for `simple-spark-databricks` but done in a
 * slightly more general way.
 */
lazy val `simple-spark-provided` = project
  .dependsOn(`simple-spark-submit`)
  .settings(
    commonSettings,
    assemblySettings
  )
  .settings(
    excludeDependencies ++= Seq(library.typesafeConfig).map { mid =>
      ExclusionRule(
        organization = mid.organization,
        name         = mid.name
      )
    }
  )

/** Several select examples from spark-examples included in Spark packages. */
lazy val `spark-examples` = project
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

/** Utilities for running Spark. */
lazy val `spark-runner` = project
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

/** Just trying couple things with CSV and Spark. */
lazy val `x-csv` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

/** Experiments with graphx. */
lazy val `x-graphx` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

/** Trying graphx examples from packt's "Scala and Spark for Big Data Analytics". */
lazy val `x-graphx-packt` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

// List of projects for 'assemblies' task
lazy val assemblyProjects = List(
  `simple-spark-submit`,
  `simple-spark-databricks`,
  `simple-spark-provided`
)

//
// Dependencies & Settings
//

lazy val library = new {

  val versions = new {
    // val scala211       = "2.11.12"
    val scala212       = "2.12.20"
    val scala213       = "2.13.15"
    val spark24        = "2.4.8"
    val spark30        = "3.0.3"
    val spark31        = "3.1.3"
    val spark32        = "3.2.4"
    val spark33        = "3.3.2"
    val spark34        = "3.4.4" // Oct 27 2024
    val spark35        = "3.5.3" // Sep 24 2024
    val spark40        = "4.0.0-preview2" // 2024-09-16
    val scalatest      = "3.2.19"
    val typesafeConfig = "1.4.3"
  }

  val supportedScalaVersions = List(versions.scala212, versions.scala213)

  private val sparkLibs = Seq("core", "sql", "graphx")
  val spark32provided = sparkLibs.map { lib =>
    "org.apache.spark" %% s"spark-${lib}" % versions.spark32 % "provided"
  }
  val spark3 = sparkLibs
    .map { lib => "org.apache.spark" %% s"spark-${lib}" % versions.spark35 }
  val spark3provided = spark3.map { _ % "provided" }
  val spark4 = sparkLibs
    .map { lib => "org.apache.spark" %% s"spark-${lib}" % versions.spark40 }
  val spark4provided = spark4.map { _ % "provided" }

  val scalatest      = "org.scalatest" %% "scalatest" % versions.scalatest
  val typesafeConfig = "com.typesafe"   % "config"    % versions.typesafeConfig

}

/**
 * Run SBT task [+assemblies] to generate assembly JARs for all projects listed in
 * [[assemblyProjects]] and for all Scala versions listed in
 * [[library.supportedScalaVersions]].
 */
val assemblyProjectFilter =
  settingKey[ScopeFilter.ProjectFilter](
    "Project filter for projects in assemblyProjects."
  )
assemblyProjectFilter := inProjects(assemblyProjects.map(_.project): _*)
val assemblies =
  taskKey[Seq[java.io.File]](
    "Task to creates assembly JAR for projects in assemblyProjects."
  )
assemblies := Def.taskDyn {
  assembly.all(ScopeFilter(assemblyProjectFilter.value))
}.value

/**
 * Add these settings to projects for which assembly JARs are supposed to be
 * generated (like those listed in [[assemblyProjects]]).
 */
lazy val assemblySettings = List(
  assembly / assemblyOption ~= { _.withIncludeScala(includeScala = false) },
  assembly / assemblyJarName :=
    s"${name.value}-assembly_${scalaBinaryVersion.value}-${version.value}.jar"
)

/**
 * In order to [runMain] in SBT we need to put "provided" dependencies (like Spark)
 * back to run classpath:
 */
lazy val runWithProvidedSettings = List(
  Compile / run := Defaults
    .runTask(
      Compile / fullClasspath,
      Compile / run / mainClass,
      Compile / run / runner
    )
    .evaluated,
  Compile / runMain := Defaults
    .runMainTask(Compile / fullClasspath, Compile / run / runner)
    .evaluated
)

lazy val commonSettings = List(
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
  javacOptions ++= Seq("-source", "17", "-target", "17")
)

// OLD:
//ThisBuild / useCoursier := false
//ThisBuild / resolvers += Resolver.mavenCentral
//ThisBuild / resolvers += Resolver.sbtPluginRepo("releases")
