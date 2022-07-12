package iskra.xgraphx.guide

import iskra.runner.{ RunnerInputSparkConfig, SparkRunner }
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{ Edge, Graph, VertexId, VertexRDD }
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object TestMain_01_GraphX_Overview {
  def main(args: Array[String]): Unit = {
    val sr: SparkRunner = SparkRunner(risc =
      RunnerInputSparkConfig(master = Some("local[*]"), sparkStopWhenDone = true)
    )

    run1(sr)

    sr.stopSpark()
  }

  /*
  In some cases it may be desirable to have vertices with different property types
  in the same graph. This can be accomplished through inheritance.
  For example to model users and products as a bipartite graph we might do the following:
  class VertexProperty()
  case class UserProperty(name: String) extends VertexProperty
  case class ProductProperty(name: String, price: Double) extends VertexProperty
   // The graph might then have the type:
    var graph: Graph[VertexProperty, String] = null
   */

  def run1(sr: SparkRunner): Unit = {
    import sr.spark.implicits._
    val sc: SparkContext = sr.spark.sparkContext

    // Create an RDD for the vertices
    val users: RDD[(VertexId, (String, String))] = sc.parallelize(
      Seq(
        (3L, ("rxin", "student")),
        (7L, ("jgonzal", "postdoc")),
        (5L, ("franklin", "prof")),
        (2L, ("istoica", "prof"))
      )
    )
    // Create an RDD for edges
    val relationships: RDD[Edge[String]] = sc.parallelize(
      Seq(
        Edge(3L, 7L, "collab"),
        Edge(5L, 3L, "advisor"),
        Edge(2L, 5L, "colleague"),
        Edge(5L, 7L, "pi")
      )
    )
    // Define a default user in case there are relationship with missing user
    val defaultUser = ("John Doe", "Missing")
    // Build the initial Graph
    val graph: Graph[(String, String), String] =
      Graph(vertices = users, edges = relationships, defaultVertexAttr = defaultUser)

    ppGraph(graph)

    // Count all users which are postdocs
    println(
      "postdoc cnt: " +
        graph.vertices.filter { case (_, (_, pos)) => pos == "postdoc" }.count()
    )
    // Count all the edges where src > dst
    println(
      "non-canonical edges cnt: " +
        graph.edges.filter(e => e.srcId > e.dstId).count()
    )

    // Use the triplets view to create an RDD of facts.
    val facts: RDD[String] = graph.triplets.map { triplet =>
      s"${triplet.srcAttr._1}(${triplet.srcId}) is the ${triplet.attr}" +
        s" of ${triplet.dstAttr._1}(${triplet.dstId})"
    }
    facts.collect().foreach(println)

    // Use the implicit GraphOps.inDegrees operator.
    // Note: Vertices with no in-edges are not returned in the resulting RDD.
    val inDegrees: VertexRDD[Int] = graph.inDegrees
    inDegrees.toDF("vid", "inDegree").show()

//    graph.joinVertices

  }

  def ppGraph(graph: Graph[_, _]): Unit = {
    val sparkSession: SparkSession = SparkSession.active
    println(
      "ppGraph: SparkSession.active.sparkContext.applicationId=" +
        s"${sparkSession.sparkContext.applicationId}"
    )
    import sparkSession.implicits._
    graph.vertices
      .map { case (id, attr) => (id, attr.toString) }
      .toDF("id", "attr")
      .sort("id")
      .show()
    graph.edges
      .map { case Edge(srcId, dstId, attr) =>
        Edge[String](srcId, dstId, attr.toString)
      }
      .toDF()
      .sort("srcId", "dstId")
      .show()
  }

}
