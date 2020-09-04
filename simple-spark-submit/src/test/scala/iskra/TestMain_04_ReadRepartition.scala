package iskra

import org.apache.spark.sql.{ Dataset, SparkSession }
import org.apache.spark.rdd.RDD
import iskra.SparkUtils.DataLocation._
import iskra.partitioner.CustomPartitionerII, CustomPartitionerII._

object TestMain_04_ReadRepartition {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkUtils.startSpark()
    run1(spark)
    spark.stop()
  }

  val srcPath: String = base + "partitioner/single/"

  def run1(spark: SparkSession): Unit = {
    import spark.implicits._
    import org.apache.spark.sql.functions.expr

    val sds: Dataset[RunData] = spark.read.parquet(srcPath).as[RunData]
    println("count=" + sds.count)
    sds.show(truncate                                = false)
    sds.mapPartitions(RunData.grByCid).show(truncate = false)

    // count records for each cid
    val cidCntSDS: Dataset[CidCnt] =
      sds.groupBy($"id.cid").agg(expr("COUNT(1) cnt")).as[CidCnt]
    val cidCnts: Seq[CidCnt] = cidCntSDS.collect.toList

    // get partitioner based on counts per cid
    val partitioner = CustomPartitionerII(cidCnts = cidCnts, cntPerPartition = 50000)
    println("numMap = " + partitioner.numMap)
    println("numPartitions = " + partitioner.numPartitions)

    // convert to pair RDD and repartition
    val rdd: RDD[(StructuredID, RunData)] =
      sds.rdd.map { runData => (runData.id, runData) }
    val rddPartitionBy                   = rdd.partitionBy(partitioner = partitioner)
    val sdsPartitionBy: Dataset[RunData] = rddPartitionBy.map(_._2).toDS
    //sdsPartitionBy.show(truncate                                = false)
    sdsPartitionBy
      .mapPartitions(RunData.grByCid)
      .show(truncate = false, numRows = partitioner.numPartitions)

    write(sds = sdsPartitionBy, isSinglePartitionDir = true, path  = pathSingle)
    write(sds = sdsPartitionBy, isSinglePartitionDir = false, path = pathMulti)
  }

}
