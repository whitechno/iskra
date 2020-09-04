package iskra.partitioner

import org.apache.spark.Partitioner
import iskra.SparkUtils.DataLocation
import iskra.StructuredID
import CustomPartitionerII._

class CustomPartitionerII(cidToNumMap: Map[Int, Int]) extends Partitioner {
  def numMap: Map[Int, Int]                = cidToNumMap
  private val cidBinsMap: Map[Int, CidBin] = buildCidBinsMap(cidToNumMap)
  private val totNum: Int                  = cidToNumMap.values.sum
  override def numPartitions: Int          = totNum

  override def getPartition(key: Any): Int =
    key match {
      case k: StructuredID =>
        cidBinsMap
          .getOrElse(k.cid, defaultCidBin(k.cid))
          .getPartition(key)
    }

  // in case cid is not in cidBinsMap
  private def defaultCidBin(cid: Int) = {
    val r = new java.util.Random(cid)
    CidBin(
      cid = cid,
      idx = r.nextInt(totNum),
      num = DEFAULT_NUM_PARTITIONS_PER_CID
    )
  }
}

object CustomPartitionerII {

  val RECORDS_PER_PARTITION: Long         = 50000
  val MAX_TOT_NUM_PARTITIONS: Int         = 3500 // do we need it?
  val DEFAULT_NUM_PARTITIONS_PER_CID: Int = 1
  val pathSingle: String                  = DataLocation.base + "partitioner-ii/single/"
  val pathMulti: String                   = DataLocation.base + "partitioner-ii/multi/"

  def apply(
      cidCnts: Seq[CidCnt],
      cntPerPartition: Long = RECORDS_PER_PARTITION
  ): CustomPartitionerII =
    // TODO: what if totNum > MAX_TOT_NUM_PARTITIONS ?
    new CustomPartitionerII(
      cidToNumMap = cidCnts.map { cc => cc.cid -> cc.numPartitions(cntPerPartition) }.toMap
    )

  case class CidCnt(cid: Int, cnt: Long) {
    def numPartitions(cntPerPartition: Long = RECORDS_PER_PARTITION): Int = {
      val num: Long = cnt / cntPerPartition
      if (num < 1L) 1 else num.toInt + 1
    }
  }

  private[partitioner] case class CidBin(cid: Int = -1, idx: Int = 0, num: Int = 0) {
    def getPartition(key: Any): Int = idx + nonNegativeMod(x = key.hashCode, mod = num)
    private def nonNegativeMod(x: Int, mod: Int): Int = {
      val rawMod = x % mod
      rawMod + (if (rawMod < 0) mod else 0)
    }
  }

  private[partitioner] def buildCidBinsMap(
      cidToNumMap: Map[Int, Int]
  ): Map[Int, CidBin] =
    cidToNumMap.toSeq
      .sortBy(-_._2)
      .scanLeft(z = CidBin()) {
        case (b, (cid, num)) =>
          CidBin(cid = cid, idx = b.idx + b.num, num = num)
      }
      .tail
      .map { cb => cb.cid -> cb }
      .toMap

}

object TestMain_CidBins extends App {
  val m = Map(101 -> 2, 102 -> 29, 103 -> 100, 104 -> 10)
  println(buildCidBinsMap(m))
}
