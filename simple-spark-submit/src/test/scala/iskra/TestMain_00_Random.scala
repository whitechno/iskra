package iskra

import java.util.Random

// simple-spark-submit / Test / runMain iskra.TestMain_00_Random
object TestMain_00_Random extends App {

  import scala.util.{ Properties => Props }
  println(
    s"\n\t*** " +
      s"(Scala ${Props.versionNumberString}, Java ${Props.javaVersion})" +
      s" ***\n"
  )

  val seed  = 1
  val bound = 10
  val r     = new Random(seed)
  (1 to 10).foreach { i => println(f"$i%2d: ${r.nextInt(bound)}") }

  val r2 = new Random(seed)
  (1 to 10).foreach { i =>
    println(f"$i%2d: ${r2.nextInt(bound)}, ${r2.nextGaussian}")
  }

  val ct: Long = java.time.Instant.now.getEpochSecond
  println("ct = " + ct)
}
