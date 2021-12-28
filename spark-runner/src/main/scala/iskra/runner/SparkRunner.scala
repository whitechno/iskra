package iskra.runner

import org.apache.spark.sql.SparkSession
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.SparkContext

case class SparkRunner(risc: RunnerInputSparkConfig = RunnerInputSparkConfig()) {

  val spark: SparkSession = startSpark()

  private def startSpark(): SparkSession = {
    // stop outputting INFO and WARN to reduce log verbosity
    if (risc.logErrorLevelOnly)
      Logger.getLogger("org.apache").setLevel(Level.ERROR)

    var builder: SparkSession.Builder = SparkSession.builder()
    risc.master.foreach { master => builder = builder.master(master) }
    risc.appName.foreach { name => builder = builder.appName(name = name) }

    builder = builder.config("spark.ui.enabled", "false")
    builder = builder.config("spark.driver.allowMultipleContexts", "false")

    val sparkSession: SparkSession = builder.getOrCreate()
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
