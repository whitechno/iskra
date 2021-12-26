ThisBuild / version      := "0.1.1" //-SNAPSHOT
ThisBuild / organization := "com.github.whitechno.iskra"
ThisBuild / scalaVersion := library.versions.scala213

/* assembly JAR for spark-submit:
Spark libraries are "provided", but typesafeConfig has to be included
 */
lazy val `simple-spark-submit` = project
  .settings(
    commonSettings,
    assemblySettings,
    libraryDependencies ++= library.spark3provided,
    libraryDependencies += library.typesafeConfig,
    // In order to run in SBT (as opposed to using 'spark-submit')
    // sbt> simple-spark-submit / runMain iskra.SimpleApp local[4]
    // we need to put "provided" Spark dependencies back to run classpath:
    runWithProvidedSettings
  )

/* assembly JAR for Databricks:
completely derived from `simple-spark-submit`
but with typesafeConfig excluded (it is provided in Databricks Spark env).
 */
lazy val `simple-spark-databricks` = project
  .dependsOn(`simple-spark-submit`)
  .settings(
    commonSettings,
    assemblySettings
  )
  .settings(
    excludeDependencies +=
      ExclusionRule(
        organization = library.typesafeConfig.organization,
        name         = library.typesafeConfig.name
      )
  )

/* assembly JAR with some `simple-spark-submit` dependencies excluded
the result is exactly the same assembly JAR as for `simple-spark-databricks`
but done in a slightly more general way
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

lazy val `spark-runner` = project
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

lazy val `x-csv` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

lazy val `x-graphx` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark3provided
  )

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
    val scala211       = "2.11.12"
    val scala212       = "2.12.15"
    val scala213       = "2.13.7"
    val spark24        = "2.4.8"
    val spark30        = "3.0.3"
    val spark31        = "3.1.2"
    val spark32        = "3.2.0"
    val scalatest      = "3.2.10"
    val typesafeConfig = "1.4.1"
  }

  val supportedScalaVersions = List(versions.scala211, versions.scala212)

  private val sparkLibs = Seq("core", "sql", "graphx")
  val spark3 = sparkLibs
    .map { lib => "org.apache.spark" %% s"spark-${lib}" % versions.spark32 }
  val spark3provided = spark3.map { _ % "provided" }

  val scalatest      = "org.scalatest" %% "scalatest" % versions.scalatest
  val typesafeConfig = "com.typesafe"   % "config"    % versions.typesafeConfig

}

/* Use SBT task
sbt> assemblies
to generate assembly JARs for all projects listed in assemblyProjects
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

// add these settings to projects for which assembly JARs
// are supposed to be generated
lazy val assemblySettings = List(
  // assembly / test := {},
  assembly / assemblyOption ~= { _.withIncludeScala(includeScala = false) },
  assembly / assemblyJarName :=
    s"${name.value}-assembly_${scalaBinaryVersion.value}-${version.value}.jar"
)

// In order to run in SBT we need to put "provided"
// dependencies back to run classpath:
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
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")
)

//ThisBuild / useCoursier := false
ThisBuild / resolvers += Resolver.mavenCentral
ThisBuild / resolvers += Resolver.sbtPluginRepo("releases")
