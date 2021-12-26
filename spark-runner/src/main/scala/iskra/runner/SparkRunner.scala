package iskra.runner

import org.apache.spark.sql.{ Dataset, SparkSession }
import org.apache.spark.sql.functions.expr
import org.apache.log4j.{ Level, Logger }

case class SparkRunner(risc: RunnerInputSparkConfig = RunnerInputSparkConfig()) {

  val spark: SparkSession = startSpark()

  private def startSpark(): SparkSession = {
    // stop outputting INFO and WARN to reduce log verbosity
    if (risc.logErrorLevelOnly)
      Logger.getLogger("org.apache").setLevel(Level.ERROR)

    var builder: SparkSession.Builder = SparkSession.builder
    risc.master.foreach { master => builder = builder.master(master) }
    risc.appName.foreach { name => builder = builder.appName(name = name) }

    builder = builder.config("spark.ui.enabled", "false")
    builder = builder.config("spark.driver.allowMultipleContexts", "false")

    val sparkSession: SparkSession = builder.getOrCreate

    println(
      "\n\t*** Spark " +
        util.Properties.versionNumberString + "/" + sparkSession.version +
        " running on " +
        sparkSession.conf.get(key = "spark.master", default = "-") +
        " with " + sparkSession.sparkContext.defaultParallelism + " cores" +
        " ***\n" +
        sparkSession.sparkContext.uiWebUrl
          .map("\tuiWebUrl at " + _ + "\n")
          .getOrElse("")
    )

    sparkSession
  }

  def stopSpark(): Unit =
    if (risc.sparkStopWhenDone) {
      println("\n\t*** Stopping Spark Session ***\n")
      spark.stop()
    }

}
