package iskra

import java.util.Random
import java.time.Instant

case class StructuredID(
    cid: Int,
    clid: Seq[Int],
    sid: Int,
    ct: Int
)

object StructuredID {
  def apply(cid: Int): StructuredID =
    StructuredID(
      cid  = cid,
      clid = Seq.fill(4)(RandomGen.nextInt),
      sid  = RandomGen.nextInt,
      ct   = Instant.now.getEpochSecond.toInt
    )
}

case class RunData(id: StructuredID, gauss: Double)

object RunData {
  def apply(cid: Int): RunData =
    RunData(
      id    = StructuredID(cid),
      gauss = RandomGen.nextGaussian
    )
  def grByCid(iter: Iterator[RunData]): Iterator[Map[Int, Int]] =
    Iterator(
      iter.toSeq.groupBy(_.id.cid).map { case (k, vs) => k -> vs.length }
    )
}

object RandomGen {
  private val r            = new Random()
  def nextInt: Int         = r.nextInt
  def nextGaussian: Double = r.nextGaussian()
}

private object TestMain_01_StructuredID extends App {
  val cids = Seq(101, 102)
  cids.foreach(cid => println(StructuredID(cid)))
}

private object TestMain_02_RunData extends App {
  val cids = Seq(101, 102)
  cids.foreach(cid => println(RunData(cid)))
}
