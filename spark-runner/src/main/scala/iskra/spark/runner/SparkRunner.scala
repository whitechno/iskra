package iskra.spark.runner

import org.apache.spark.sql.SparkSession
import org.apache.spark.{ SparkConf, SparkContext }
//import org.apache.log4j.{ Level, Logger }
//import org.apache.logging.{ log4j => log4j2 }

case class SparkRunner(risc: RunnerInputSparkConfig = RunnerInputSparkConfig()) {

  val spark: SparkSession = startSpark()

  private val sparkConf = new SparkConf()
  if (!sparkConf.getAll.isEmpty)
    println(s"*** conf: ${sparkConf.getAll.mkString("\n", "\n", "")}")

  private def startSpark(): SparkSession = {
    // stop outputting INFO and WARN to reduce log verbosity
//    if (risc.logErrorLevelOnly) {
//      Logger.getLogger("org.apache").setLevel(Level.ERROR)
//      LoggerFactory.getLogger("org.apache")

//      println(log4j2.LogManager.getLogger("org.apache").getLevel)
//      log4j2.core.config.Configurator
//        .setLevel("org.apache", log4j2.Level.ERROR)
//      println(log4j2.LogManager.getLogger("org.apache").getLevel)
//    }
    var builder: SparkSession.Builder = SparkSession.builder()
    risc.master.foreach { master => builder = builder.master(master) }
    risc.appName.foreach { name => builder = builder.appName(name = name) }

    builder = builder.config("spark.ui.enabled", "false")
    builder = builder.config("spark.driver.allowMultipleContexts", "false")

    /*
spark.executor.extraJavaOptions -XX:+PrintGCDetails -Dkey=value -Dnumbers="one two"

from spark-env.sh
# Options for launcher
# - SPARK_LAUNCHER_OPTS, to set config properties and Java options for the launcher
(e.g. "-Dx=y")
# - SPARK_CONF_DIR      Alternate conf dir. (Default: ${SPARK_HOME}/conf)
     */

    // println(log4j2.LogManager.getLogger("org.apache").getLevel)
    val sparkSession: SparkSession = builder.getOrCreate()
    // println(log4j2.LogManager.getLogger("org.apache").getLevel)

    val sparkContext: SparkContext = sparkSession.sparkContext
    // sparkSession.conf.get(key = "spark.master", default = "-")
    val sparkMaster: String     = sparkContext.master
    val defaultParallelism: Int = sparkContext.defaultParallelism

    println(
      s"\n\t*** Spark ${sparkSession.version} " +
        s"(Scala ${util.Properties.versionNumberString})" +
        s" running on ${sparkMaster} with ${defaultParallelism} cores ***\n" +
        s"\t    applicationId=${sparkContext.applicationId}\n" +
        sparkContext.uiWebUrl.map("\t    uiWebUrl at " + _ + "\n").getOrElse("")
    )

    sparkSession
  }

  def stopSpark(): Unit = if (risc.sparkStopWhenDone) {
    println("\n\t*** Stopping Spark Session ***\n")
    spark.stop()
  }

}
