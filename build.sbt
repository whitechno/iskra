ThisBuild / version      := "0.1.1" //-SNAPSHOT
ThisBuild / organization := "com.github.whitechno.iskra"
ThisBuild / scalaVersion := library.versions.scala212

/* assembly JAR for spark-submit:
Spark libraries are "provided", but typesafeConfig has to be included
 */
lazy val `simple-spark-submit` = project
  .settings(
    commonSettings,
    assemblySettings,
    libraryDependencies ++= library.spark30provided,
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
but done in a slightly more general wasy
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
    libraryDependencies ++= library.spark30provided
  )

lazy val `x-graphx` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark30provided
  )

lazy val `x-csv` = project
  .dependsOn(`spark-runner`)
  .settings(
    commonSettings,
    libraryDependencies ++= library.spark30provided
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
    val scala212       = "2.12.12"
    val spark24        = "2.4.7"
    val spark30        = "3.0.1"
    val spark31        = "3.1.0-rc1"
    val scalatest      = "3.2.3"
    val typesafeConfig = "1.4.1"
  }

  val supportedScalaVersions = List(versions.scala211, versions.scala212)

  private val sparkLibs = Seq("core", "sql", "graphx")
  val spark30 = sparkLibs
    .map { lib => "org.apache.spark" %% s"spark-${lib}" % versions.spark30 }
  val spark30provided = spark30.map { _ % "provided" }
  val spark31 = sparkLibs
    .map { lib => "org.apache.spark" %% s"spark-${lib}" % versions.spark31 }
  val spark31provided = spark31.map { _ % "provided" }

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
  test in assembly := {},
  assemblyOption in assembly :=
    (assemblyOption in assembly).value.copy(includeScala = false),
  assemblyJarName in assembly :=
    s"${name.value}-assembly_${scalaBinaryVersion.value}-${version.value}.jar"
)

// In order to run in SBT we need to put "provided"
// dependencies back to run classpath:
lazy val runWithProvidedSettings = List(
  run in Compile := Defaults
    .runTask(
      fullClasspath in Compile,
      mainClass in (Compile, run),
      runner in (Compile, run)
    )
    .evaluated,
  runMain in Compile := Defaults
    .runMainTask(fullClasspath in Compile, runner in (Compile, run))
    .evaluated
)

lazy val commonSettings = List(
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature"
  )
)

ThisBuild / useCoursier := false
ThisBuild / resolvers += Resolver.mavenCentral
ThisBuild / resolvers += Resolver.sbtPluginRepo("releases")
