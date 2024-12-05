package iskra

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ Dataset, SparkSession }
import SparkUtils._
import SparkUtils.DataLocation._
import partitioner.CustomPartitionerI, CustomPartitionerI._

// simple-spark-submit / Test / runMain iskra.TestMain_03_CreateWrite
object TestMain_03_CreateWrite {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkUtils.startSpark()
    run_01(spark)
    // run_02_write(spark)
    spark.stop()
  }

  def run_01(spark: SparkSession): Unit = {
    import spark.implicits._
    import org.apache.spark.sql.functions.expr

    val rdd: RDD[(StructuredID, RunData)] = spark.sparkContext
      .parallelize(for {
        cid <- cidToNumMap.keySet.toSeq
        cnt <- cidToNumMap.get(cid).map(_ * 70000).toSeq
        rd  <- Seq.fill(cnt)(RunData(cid = cid))
      } yield (rd.id, rd))
    val sds: Dataset[RunData] = rdd.map(_._2).toDS()

    println("count=" + rdd.cache().count())
    println("partitioner=" + rdd.partitioner)
    println("NumPartitions=" + rdd.getNumPartitions)
    println("rdd: " + countByPartition(rdd).collect().toList)
    sds.show(truncate                                = false)
    sds.mapPartitions(RunData.grByCid).show(truncate = false)
    val grByCtSDF = sds.groupBy($"id.ct").agg(expr("COUNT(1) cnt"))
    grByCtSDF.cache().count()
    grByCtSDF.sort($"ct").show()

    val rddPartitionBy: RDD[(StructuredID, RunData)] =
      rdd.partitionBy(new CustomPartitionerI())
    val sdsPartitionBy: Dataset[RunData] = rddPartitionBy.map(_._2).toDS()

    println("count=" + rddPartitionBy.count())
    println("partitioner=" + rddPartitionBy.partitioner)
    println("NumPartitions=" + rddPartitionBy.getNumPartitions)
    println("rdd: " + countByPartition(rddPartitionBy).collect().toList)
    sdsPartitionBy.show(truncate                                = false)
    sdsPartitionBy.mapPartitions(RunData.grByCid).show(truncate = false)

    write(sds = sdsPartitionBy, isSinglePartitionDir = true, path  = pathSingle)
    write(sds = sdsPartitionBy, isSinglePartitionDir = false, path = pathMulti)
  }

  def run_02_write(spark: SparkSession): Unit = {
    import spark.implicits._

    val sds: Dataset[RunData] = spark.sparkContext
      .parallelize(for {
        cid <- cidToNumMap.keySet.toSeq
        cnt <- cidToNumMap.get(cid).map(_ * 70000).toSeq
        rd  <- Seq.fill(cnt)(RunData(cid = cid))
      } yield rd)
      .toDS()

    println("count=" + sds.rdd.count())
    println("partitioner=" + sds.rdd.partitioner)
    println("NumPartitions=" + sds.rdd.getNumPartitions)
    println("rdd: " + countByPartition(sds.rdd).collect().toList)
    sds.show(truncate                                = false)
    sds.mapPartitions(RunData.grByCid).show(truncate = false)

    write(
      sds                  = sds,
      isSinglePartitionDir = true,
      path                 = DataLocation.base + "partitioner/single/"
    )
    write(
      sds                  = sds,
      isSinglePartitionDir = false,
      path                 = DataLocation.base + "partitioner/multi/"
    )
  }

}
