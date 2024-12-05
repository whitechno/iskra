package iskra

import org.apache.spark.sql.SparkSession

/* simple-spark-submit / Test / runMain iskra.TestMain_01_SparkSetup
*** Spark 4.0.0-preview2 (Scala 2.13.15, Java 17.0.13) running on local[*] with 16 cores ***
 */
object TestMain_01_SparkSetup {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkUtils.startSpark()
    run1()
    run2()
    spark.stop()
  }

  def run1(): Unit = {
    val spark = SparkSession.active
    import spark.implicits._
    val sourceDF = Seq(
      ("jets", "football"),
      ("nacional", "soccer")
    ).toDF("team", "sport").as[(String, String)]
    sourceDF.show()
  }

  private case class Run2Class(team: String, sport: String)
  def run2(): Unit = {
    val spark = SparkSession.active
    import spark.implicits._

    val sourceDF = Seq(
      ("jets", "football"),
      ("nacional", "soccer")
    ).toDF(colNames = "team", "sport").as[Run2Class]
    sourceDF.show()
  }
}
