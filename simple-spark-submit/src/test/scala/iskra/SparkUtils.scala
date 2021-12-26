package iskra

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ Dataset, SparkSession }
import org.apache.spark.sql.functions.expr
import org.apache.log4j.{ Level, Logger }

object SparkUtils {

  /**
   * master(master: String) Sets the Spark master URL to connect to, such as "local"
   * to run locally, "local[4]" to run locally with 4 cores, or "spark://master:7077"
   * to run on a Spark standalone cluster.
   *
   * appName(name: String) Sets a name for the application, which will be shown in
   * the Spark web UI. If no application name is set, a randomly generated name will
   * be used.
   *
   * Use spark.sparkContext.uiWebUrl.foreach(url => println("uiWebUrl at " + url)) to
   * print the entire URL of a Spark application’s web UI.
   */
  def startSpark(
      master: String             = "local[*]",
      appName: String            = "Iskra",
      logErrorLevelOnly: Boolean = true
  ): SparkSession = {
    // stop outputting INFO and WARN to reduce log verbosity
    if (logErrorLevelOnly)
      Logger.getLogger("org.apache").setLevel(Level.ERROR)

    val spark = SparkSession.builder
      .master(master = master)
      .appName(name = appName)
      // .config("spark.ui.showConsoleProgress", value = false)
      .getOrCreate
    spark.sparkContext.uiWebUrl.foreach { url => println("uiWebUrl at " + url) }
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
    private val root   = "/Users/owhite/"
    val base: String = access + root +
      "dev-data/whitechno-github/spica/iskra/"

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
