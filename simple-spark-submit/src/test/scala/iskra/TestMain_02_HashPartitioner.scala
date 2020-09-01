package iskra

//import java.util.Random
//import org.apache.hadoop.conf.Configuration
//import org.apache.hadoop.fs.Path
//import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat
//import org.apache.parquet.hadoop.ParquetOutputFormat
//import org.apache.parquet.hadoop.metadata.CompressionCodecName

import org.apache.spark.rdd.RDD
import org.apache.spark.{ HashPartitioner, Partitioner, SparkContext }
import org.apache.spark.sql.{ Dataset, SparkSession }
import SparkUtils._

object TestMain_02_HashPartitioner {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkUtils.startSpark()
    val sc: SparkContext    = spark.sparkContext
    run1(sc)
    run2(spark)
    spark.stop()
  }

  private case class Run2Data(x: Int, y: Int)
  def run2(spark: SparkSession): Unit = {
    import spark.implicits._

    val data: IndexedSeq[Run2Data] = for {
      x <- 1 to 3
      y <- 1 to 7
    } yield Run2Data(x, y)
    val sds: Dataset[Run2Data] = data.toDS
    sds.show(numRows = data.length)

    val rdd: RDD[(Int, Run2Data)] = sds.rdd.map { r2d => (r2d.x, r2d) }
    println("partitioner=" + rdd.partitioner)
    println("NumPartitions=" + rdd.getNumPartitions)
    println("rdd: " + countByPartition(rdd).collect.toList)

    val rddOnePartition: RDD[Run2Data] = rdd
      .partitionBy(new HashPartitioner(partitions = 1))
      .map(_._2)
    println(
      "rddOnePartition: " + countByPartition(rddOnePartition).collect.toList
    )

    val rddTwoPartitions: RDD[Run2Data] = rdd
      .partitionBy(new HashPartitioner(partitions = 2))
      .map(_._2)
    println("rddTwoPartitions: " + countByPartition(rddTwoPartitions).collect.toList)
    rddTwoPartitions.toDS.show(numRows = data.length)

    rddTwoPartitions.toDS.mapPartitions { iter => Iterator(iter.length) }.show

  }

  def run1(sc: SparkContext): Unit = {
    val data: IndexedSeq[(Int, Int)] = for {
      x <- 1 to 3
      y <- 1 to 7
    } yield (x, y)
    println(data)

    val rdd: RDD[(Int, Int)] = sc.parallelize(data, 7)

    println("count=" + rdd.count)
    println("partitioner=" + rdd.partitioner)
    println("NumPartitions=" + rdd.getNumPartitions)
    println("rdd: " + countByPartition(rdd).collect.toList)

    val rddOnePartition = rdd.partitionBy(new HashPartitioner(1))
    println("rddOnePartition: " + countByPartition(rddOnePartition).collect.toList)

    val rddTwoPartitions = rdd.partitionBy(new HashPartitioner(2))
    println("rddTwoPartitions: " + countByPartition(rddTwoPartitions).collect.toList)
    val rddTwoPartitionsSet: RDD[Set[Int]] = collectKeysByPartition(rddTwoPartitions)
    println("rddTwoPartitionsSet: " + rddTwoPartitionsSet.collect.toList)

    val rddSevenPartitions = rdd.partitionBy(new HashPartitioner(7))
    println(
      "rddSevenPartitions: " + countByPartition(rddSevenPartitions).collect.toList
    )
    val rddSevenPartitionsSet: RDD[Set[Int]] =
      collectKeysByPartition(rddSevenPartitions)
    println("rddSevenPartitionsSet: " + rddSevenPartitionsSet.collect.toList)
  }
}
