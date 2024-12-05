package iskra.xcsv

import iskra.spark.runner.{ RunnerInputSparkConfig, SparkRunner }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ DataFrame, SaveMode }

/* x-csv / Test / runMain iskra.xcsv.TestMain_02_CSV_RDD_replace
See test/resources/log4j2.properties
	*** Spark 3.5.3 (Scala 2.13.15) running on local[*] with 16 cores ***
	    applicationId=local-1733351473799
 */
object TestMain_02_CSV_RDD_replace {
  def main(args: Array[String]): Unit = {
    val risc =
      RunnerInputSparkConfig(master = Some("local[*]"), sparkStopWhenDone = true)
    val sr: SparkRunner = SparkRunner(risc = risc)

    // runRepairCSV(sr)
    runReadCSV(sr)

    sr.stopSpark()
  }

  def runReadCSV(sr: SparkRunner): Unit = {

    val fromSDF: DataFrame = readCSV(sr, fileName = "from.csv")
    fromSDF.show(truncate = false)

    val toSDF: DataFrame = readCSV(sr, fileName = "to.csv")
    toSDF.show(truncate = false)

  }

  def runRepairCSV(sr: SparkRunner): Unit = {
    import sr.spark.implicits._

    val fromRDD = readTextFile(sr, fileName = "from.csv").coalesce(numPartitions = 1)
    fromRDD.toDF().show(truncate = false)

    // repair csv
    val toRDD = fromRDD.map(CsvRepair.repairCsv)

    val toPath = dataPath + "to.csv.out"
    // fromRDD.saveAsTextFile(toPath)
    toRDD.toDF().write.mode(saveMode = SaveMode.Overwrite).text(toPath)

  }

  def readTextFile(sr: SparkRunner, fileName: String): RDD[String] = {
    val fromPath = dataPath + fileName
    sr.spark.sparkContext.textFile(fromPath)
  }

  def readCSV(sr: SparkRunner, fileName: String): DataFrame = {
    val fromPath = dataPath + fileName
    sr.spark.read
      .options(
        Map(
          "header" -> "true"
        )
      )
      .csv(fromPath)
  }

  private val dataPath: String =
    System.getProperty("user.dir") + "/x-csv/data/neu/csv-replace/"

}
