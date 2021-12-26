package iskra

import java.util.Random

object TestMain_00_Random extends App {
  val seed  = 1
  val bound = 10
  val r     = new Random(seed)
  (1 to 10).foreach { i => println(s"$i: ${r.nextInt(bound)}") }

  val r2 = new Random(seed)
  (1 to 10).foreach { i => println(s"$i: ${r2.nextInt(bound)}, ${r2.nextGaussian}") }

  val ct: Long = java.time.Instant.now.getEpochSecond
  println("ct = " + ct)
}
