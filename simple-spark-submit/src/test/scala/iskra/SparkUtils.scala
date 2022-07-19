package iskra

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.sql.{ Dataset, SparkSession }
import org.apache.spark.sql.functions.expr
import org.slf4j.{ Logger, LoggerFactory }

object SparkUtils {
  @transient private var _log: Logger = _ // null
  private val logName: String         = getClass.getName.stripSuffix("$")

  private def log: Logger = {
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

  /**
   * master(master: String) Sets the Spark master URL to connect to, such as "local"
   * to run locally, "local[4]" to run locally with 4 cores, or "spark://master:7077"
   * to run on a Spark standalone cluster.
   */
  def startSpark(
      master: String  = "local[*]",
      appName: String = "Iskra"
  ): SparkSession = {

    val spark = SparkSession
      .builder()
      .master(master = master)
      .appName(name = appName)
      .config("spark.ui.enabled", "false")
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext
    log.info(
      s"\n\t*** Spark ${spark.version} " +
        s"(Scala ${util.Properties.versionNumberString})" +
        s" running on ${sc.master} with ${sc.defaultParallelism} cores ***\n"
    )

    spark
  }

  /** Counts the size of each RDD partition. */
  def countByPartition(rdd: RDD[_]): RDD[Int] =
    rdd.mapPartitions { iter => Iterator(iter.length) }

  /** Collects all distinct keys at each partition. */
  def collectKeysByPartition[K, V](rdd: RDD[(K, V)]): RDD[Set[K]] =
    rdd.mapPartitions { iter => Iterator(iter.map(_._1).toSet) }

  /** Dastaset write utils */
  object DataLocation {

    private val access = ""
    private val root   = sys.env.getOrElse("DEV_DATA", "")
    val base: String   = access + root + "/whitechno-github/spica/iskra/"

    def write(
        sds: Dataset[_],
        isSinglePartitionDir: Boolean,
        path: String
    ): Unit = {
      println("\n>>> Writing to " + path + "\n")
      if (isSinglePartitionDir)
        sds.write.mode(saveMode = "overwrite").parquet(path)
      else
        sds
          .withColumn("cid", expr("id.cid"))
          .write
          .mode(saveMode = "overwrite")
          .partitionBy(colNames = "cid")
          .parquet(path)
    }
  }

}
