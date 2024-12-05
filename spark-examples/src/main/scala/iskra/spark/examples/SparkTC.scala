package iskra.spark.examples

import scala.collection.mutable
import scala.util.Random

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/*
sbt> +spark-examples/package
$DEV/apache-github/spark/bin/spark-submit \
  --master local[4] \
  --class "iskra.spark.examples.SparkTC" \
  spark-examples/target/scala-2.13/spark-examples_2.13-0.1.2.jar 1 20 10
 */

/** Transitive closure on a graph. */
object SparkTC {
  private var numSlices: Int   = _
  private var numEdges: Int    = _
  private var numVertices: Int = _

  private val rand = new Random(42)

  private def generateRandomGraph: Seq[(Int, Int)] = {
    val edges: mutable.Set[(Int, Int)] = mutable.Set.empty
    while (edges.size < numEdges) {
      val from = rand.nextInt(numVertices)
      val to   = rand.nextInt(numVertices)
      if (from != to) edges.+=((from, to))
    }
    edges.toSeq
  }

  /* NOTE:
  TC algorithm implemented here has minor defect as it might produce self-loops,
  like 1->2,2->1 will have the following TC with two self-loops: 1->1,1->2,2->1,2->2
  The defect is easily fixable but we wanted to keep the original implementation
  from Spark's Examples.
  For example, for TriangleCycle the TC will be with three self-loops:
  1->1,1->2,2->1,2->2,2->3,3->2,3->3,3->1,1->3
   */
  private def generateTriangleCycle: Seq[(Int, Int)] = Seq(1 -> 2, 2 -> 3, 3 -> 1)
  // 1->2,2->1 => 1->1,1->2,2->1,2->2
  // 1->2,2->3 => 1->2,2->3,3->1

  def countSingleAndDoubleEdges(gr: RDD[(Int, Int)]): collection.Map[Int, Long] = gr
    .filter(x => x._1 != x._2)
    .map(x => Seq(x._1, x._2).sorted -> 1)
    .reduceByKey(_ + _)
    .map(x => x._2 -> 1)
    .countByKey()

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SparkTC")
      .getOrCreate()
    numSlices   = if (args.length > 0) args(0).toInt else 1
    numEdges    = if (args.length > 1) args(1).toInt else 20
    numVertices = if (args.length > 2) args(2).toInt else 10

    val generatedGraph =
      if (args.length == 0) generateTriangleCycle else generateRandomGraph
    var tc = spark.sparkContext.parallelize(generatedGraph, numSlices).cache()
    println(
      s"Generated graph has ${tc.count()} edges and " +
        s"${tc.flatMap { x => Seq(x._1, x._2) }.distinct().count()} vertices."
    )
    println(s"Count of single and double edges: ${countSingleAndDoubleEdges(tc)}")

    // Linear transitive closure: each round grows paths by one edge,
    // by joining the graph's edges with the already-discovered paths.
    // e.g. join the path (y, z) from the TC with the edge (x, y) from
    // the graph to obtain the path (x, z).

    // Because join() joins on keys, the edges are stored in reversed order.
    // NOTE: Generated graph has 200 edges and 95 vertices.
    //       TC has 6254 edges (with 63 self-loops) and 95 vertices.
    //       18479, 19259, 49979 milliseconds with edges.cache
    //       21790, 26870, 61969 milliseconds without edges.cache
    // val edges = tc.map(x => (x._2, x._1))
    val edges = spark.sparkContext
      .parallelize(generatedGraph, numSlices)
      .map(x => (x._2, x._1))
      .cache()
    edges.count()

    val startTime = System.nanoTime

    // This join is iterated until a fixed point is reached.
    var oldCount  = 0L
    var nextCount = tc.count()
    do {
      oldCount = nextCount
      // Perform the join, obtaining an RDD of (y, (z, x)) pairs,
      // then project the result to obtain the new (x, z) paths.
      tc = tc
        .union( // add filter to eliminate self-loops
          tc.join(edges).filter(x => x._2._2 != x._2._1).map(x => (x._2._2, x._2._1))
        )
        .distinct()
        .cache()
      nextCount = tc.count()
      println(nextCount)
    } while (nextCount != oldCount)

    println(f"Test took ${(System.nanoTime - startTime) / 1e6}%.0f milliseconds")

    println(
      s"TC has ${nextCount} edges" +
        s" (with ${tc.filter(x => x._1 == x._2).count()} self-loops) and " +
        s"${tc.flatMap { x => Seq(x._1, x._2) }.distinct().count()} vertices."
    )
    println(s"Count of single and double edges: ${countSingleAndDoubleEdges(tc)}")

    spark.stop()
  }
}
/*
See "Map-Reduce Extensions and Recursive Queries" by Afrati et al. 2011
https://web.archive.org/web/20140810063150/
http://www.edbt.org/Proceedings/2011-Uppsala/papers/edbt/a1-afrati.pdf
 */
