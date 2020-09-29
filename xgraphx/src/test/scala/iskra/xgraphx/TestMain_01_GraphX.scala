package iskra.xgraphx

//import org.apache.spark.graphx
import iskra.runner.{ RunnerInputSparkConfig, SparkRunner }

// sparkStopWhenDone = true is needed (only!) when run in sbt:
// sbt> xgraphx / test:runMain iskra.xgraphx.TestMain_01_GraphX
object TestMain_01_GraphX {
  def main(args: Array[String]): Unit = {
    val risc = RunnerInputSparkConfig(master = Some("local[*]"), sparkStopWhenDone = true)
    val sr: SparkRunner = SparkRunner(risc = risc)

    run(sr)

    sr.stopSpark()
  }

  case class User(name: String, occupation: String)

  def run(sr: SparkRunner): Unit = {
    println("Users")

  }

}
