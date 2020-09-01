package iskra

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.log4j.{ Level, Logger }

object SparkUtils {

  def startSpark(
      master: String             = "local[7]",
      appName: String            = "Iskra",
      logErrorLevelOnly: Boolean = true
  ): SparkSession = {
    // stop outputting INFO and WARN to reduce log verbosity
    if (logErrorLevelOnly)
      Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)

    SparkSession.builder
      .master(master = master)
      .appName(name = appName)
      .getOrCreate
  }

  def countByPartition(rdd: RDD[_]): RDD[Int] =
    rdd.mapPartitions { iter => Iterator(iter.length) }

  def collectKeysByPartition[T, U](rdd: RDD[(T, U)]): RDD[Set[T]] =
    rdd.mapPartitions { iter => Iterator(iter.map(_._1).toSet) }

}
