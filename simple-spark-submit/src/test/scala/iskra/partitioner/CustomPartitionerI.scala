package iskra.partitioner

import java.util.Random
import org.apache.spark.Partitioner
import iskra.SparkUtils.DataLocation
import iskra.StructuredID

object CustomPartitionerI {
  val totNum                     = 14
  val defaultCidNum              = 1
  val cidToNumMap: Map[Int, Int] = Map(101 -> 1, 102 -> 20)

  val pathSingle: String = DataLocation.base + "partitioner-i/single/"
  val pathMulti: String  = DataLocation.base + "partitioner-i/multi/"
}

class CustomPartitionerI(
    totNum: Int                = CustomPartitionerI.totNum,
    cidToNumMap: Map[Int, Int] = CustomPartitionerI.cidToNumMap
) extends Partitioner {
  override def numPartitions: Int = totNum

  override def getPartition(key: Any): Int =
    key match {
      case k: StructuredID =>
        val cid              = k.cid
        val cidNumPartitions = cidToNumMap.getOrElse(cid, CustomPartitionerI.defaultCidNum)
        val r                = new Random(cid)
        nonNegativeMod(
          x = r.nextInt(totNum) +
            nonNegativeMod(x = key.hashCode, mod = cidNumPartitions),
          mod = totNum
        )
    }

  private def nonNegativeMod(x: Int, mod: Int): Int = {
    val rawMod = x % mod
    rawMod + (if (rawMod < 0) mod else 0)
  }
}
