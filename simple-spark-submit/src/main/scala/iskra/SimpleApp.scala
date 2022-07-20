package iskra

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.slf4j.{ Logger, LoggerFactory }

/*
 # Run it in SBT with `runWithProvidedSettings` in build.sbt:
sbt> simple-spark-submit / runMain iskra.SimpleApp local[*]
 */
object SimpleApp {
  // Make the log field transient so that objects with Logging can
  // be serialized and used on another machine
  @transient private var _log: Logger = _ // null

  // Method to get the logger name for this object
  // Ignore trailing $'s in the class names for Scala objects
  protected def logName: String = this.getClass.getName.stripSuffix("$")

  // See log4j and log4j2 properties in resources
  // iskra.SimpleApp logger is set to INFO level
  protected def log: Logger = {
    if (_log == null) {
      _log = LoggerFactory.getLogger(logName)
      println(s"Logger ${logName} is set with level ${logLevel(_log)}.")
    }
    _log
  }

  private def logLevel(log: Logger): String = {
    if (log.isTraceEnabled) "TRACE"
    else if (log.isDebugEnabled) "DEBUG"
    else if (log.isInfoEnabled) "INFO"
    else if (log.isWarnEnabled) "WARN"
    else if (log.isErrorEnabled) "ERROR"
    else "FATAL"
  }

  def main(args: Array[String]): Unit = {

    // checking if there is an active Spark session
    log.warn(
      s"""\n\t~~~ ${SparkSession.getActiveSession
          .map("Active Spark session is on " + _.sparkContext.master)
          .getOrElse("No active Spark session")} ~~~\n"""
    )

    var builder = SparkSession
      .builder()
      .appName("Simple Application")
    // .config("spark.ui.enabled", "false")
    var stopSparkWhenDone = false
    if (args.length > 0) { // runMain iskra.SimpleApp local[*]
      args.zipWithIndex.foreach(println)
      val master = args(0)
      builder           = builder.master(master).config("spark.ui.enabled", "false")
      stopSparkWhenDone = true
    } // else spark-submit --master local[4]

    val spark: SparkSession = builder.getOrCreate()
    val sc: SparkContext    = spark.sparkContext

    import scala.util.{ Properties => Props }
    val osNameVersion =
      Props.osName + Props.propOrNone("os.version").map(" " + _).getOrElse("")
    log.warn(
      s"\n\t~~~ Spark ${spark.version} " +
        s"(Scala ${Props.versionNumberString}, Java ${Props.javaVersion}" +
        s", ${osNameVersion})" +
        s" on ${sc.master} with ${sc.defaultParallelism} cores ~~~\n" +
        s"\t    applicationId=${sc.applicationId}" +
        s", deployMode=${sc.deployMode}, isLocal=${sc.isLocal}\n" +
        sc.uiWebUrl.map("\t    uiWebUrl at " + _ + "\n").getOrElse("")
    )

    log.warn(
      s"""
         |-----------------------------------------------------------------------
         |>>>>>>> Spark configuration spark.sparkContext.getConf.toDebugString:
         |${sc.getConf.toDebugString}
         |-----------------------------------------------------------------------
         |""".stripMargin
    )

    /* Stopping INFO and WARN Spark messages displaying on console with
    spark.sparkContext.setLogLevel("ERROR")
    This is the easiest way to stop Spark's very verbose INFO and WARN log messages.
    However this is not a perfect solution because
    1) It sets ERROR level to rootLogger org.apache.log4j.Logger.getRootLogger(),
       so that all loggers are affected (unless they are specifically configured).
    2) There are still some INFO and WARN messages logged from builder.getOrCreate()
       before spark.sparkContext.setLogLevel("ERROR") kicks in.
     */
    val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
    println(
      s"Root Logger ${Logger.ROOT_LOGGER_NAME} is set with level ${logLevel(rootLogger)}."
    )
    sc.setLogLevel("ERROR")
    println(
      s"Root Logger ${Logger.ROOT_LOGGER_NAME} is re-set with level ${logLevel(rootLogger)}."
    )
    println(s"Logger ${logName} is re-set with level ${logLevel(_log)}.")

    /* system properties
         java.lang.System.getProperties: java.util.Properties
         scala.sys.props: scala.sys.SystemProperties
         scala.util.Properties.propOrElse(name: String, alt: => String): String

       system environment
         java.lang.System.getenv:  java.util.Map<String, String>
           ${System.getenv.entrySet().toArray.mkString("\n", "\n", "\n")}
         scala.sys.env:  Map[String, String]
         scala.util.Properties.envOrNone(name: String): String
     */
    log.debug(
      s"""
         |-----------------------------------------------------------------------
         |>>>>>>> getting system properties with scala.sys.props:
         |        ${scala.sys.props.toSeq
          .sortBy(_._1)
          .map { case (k, v) => f"$k%32s -> $v" }
          .mkString("\n", "\n", "\n")}
         |        :scala.sys.props
         |        
         |>>>>>>> getting system environment with scala.sys.env:
         |        ${scala.sys.env.toSeq
          .sortBy(_._1)
          .map { case (k, v) => f"$k%32s -> $v" }
          .mkString("\n", "\n", "\n")}
         |        :scala.sys.env
         |-----------------------------------------------------------------------
         |""".stripMargin
    )

    // to check if com.typesafe.config is available at run time:
    val userDir  = com.typesafe.config.ConfigFactory.load().getString("user.dir")
    val userHome = com.typesafe.config.ConfigFactory.load().getString("user.home")
    println(
      s"""
         |-----------------------------------------------------------------------
         |>>>>>>> user.dir
         |        com.typesafe.config.ConfigFactory.load().getString("user.dir"):
         |        $userDir
         |        
         |        java.lang.System.getProperty("user.dir"):
         |        ${System.getProperty("user.dir")}
         |        
         |        scala.util.Properties.userDir:
         |        ${util.Properties.userDir}
         |        
         |>>>>>>> user.home
         |  com.typesafe.config.ConfigFactory.load().getString("user.home"): $userHome
         |  java.lang.System.getProperty("user.home"): ${System.getProperty(
          "user.home"
        )}
         |  scala.util.Properties.userHome: ${util.Properties.userHome}
         |-----------------------------------------------------------------------
         |""".stripMargin
    )

    // run some Spark:
    // val logFile = userDir + "/README.md" // "~/dev/apache-github/spark/README.md"
    val logFile = sys.env.getOrElse("DEV", "") + "/apache-github/spark/README.md"
    val logData = spark.read.textFile(logFile).cache()
    val numAs   = logData.filter(line => line.contains("a")).count()
    val numBs   = logData.filter(line => line.contains("b")).count()
    println(
      s"""
         |-----------------------------------------------------------------------
         |>>>>>>> In ${logFile}, 
         |        lines with a: $numAs, lines with b: $numBs.
         |-----------------------------------------------------------------------
         |""".stripMargin
    )

    if (stopSparkWhenDone) {
      log.warn(s"\n\t~~~ Stopping Spark ${sc.applicationId} ~~~")
      spark.stop()
    }
  }

}
/*
 # spark-submit doesn't work if we need libraries not provided by Spark:
```
sbt> +simple-spark-submit / package
$ ~/dev/spark-bin/spark-3.0.1-bin-hadoop2.7/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit_2.12-0.1.1.jar
```
"Exception in thread "main" java.lang.NoClassDefFoundError: com/typesafe/config/ConfigFactory"

 # So, for spark-submit, if we need libraries not provided by Spark,
   like com.typesafe.config,
   then we have to generate assembly JAR with those libraries included.
   Spark-provided libraries have to be marked as '% "provided"' in build.sbt:
```
sbt> +simple-spark-submit / assembly
$ jar tvf simple-spark-submit/target/scala-2.13/\
simple-spark-submit-assembly_2.13-0.1.1.jar
$ ~/dev/spark-bin/spark-3.2.0-bin-hadoop3.2-scala2.13/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
$ ~/dev/spark-bin/spark-3.2.1-bin-hadoop3.2/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

 # For simple-spark-databricks project:
```
sbt> +simple-spark-databricks / assembly
$ jar tvf simple-spark-databricks/target/scala-2.12/\
simple-spark-databricks-assembly_2.12-0.1.1.jar
```
The `simple-spark-databricks-assembly_2.12-0.1.1.jar` JAR can be run in Databricks,
but fails with spark-submit. Try it:
```
$ ~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-databricks/target/scala-2.12/simple-spark-databricks-assembly_2.12-0.1.1.jar
```

 # just to check that JAR's content is the same:
```
sbt> +simple-spark-provided / assembly
$ jar tvf simple-spark-provided/target/scala-2.12/\
simple-spark-provided-assembly_2.12-0.1.1.jar
```
 */
