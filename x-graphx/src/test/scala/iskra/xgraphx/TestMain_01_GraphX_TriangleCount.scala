package iskra.xgraphx

import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.{
  Edge,
  EdgeContext,
  EdgeTriplet,
  Graph,
  VertexId,
  VertexRDD
}
import iskra.runner.{ RunnerInputSparkConfig, SparkRunner }

/* data/packt/BigDataAnalytics/Chapter10-GraphX
System.getProperty("user.home"), System.getProperty("user.dir")
val users: RDD[(Long, User)] = sr.spark.sparkContext.parallelize(dataUsers)

sparkStopWhenDone = true is needed (only!) when run in sbt:
sbt> x-graphx / test:runMain iskra.xgraphx.TestMain_01_GraphX_TriangleCount

    println("vertices:")
    graph.vertices.toDF("id", "attr").sort($"id").show
    println("edges:")
    graph.edges.toDF.sort($"srcId", $"dstId").show(numRows = 100)
 */
object TestMain_01_GraphX_TriangleCount {
  def main(args: Array[String]): Unit = {
    val risc =
      RunnerInputSparkConfig(master = Some("local[*]"), sparkStopWhenDone = true)
    val sr: SparkRunner = SparkRunner(risc = risc)

    run3(sr)

    sr.stopSpark()
  }

  case class User(name: String, occupation: String)

  def run3(sr: SparkRunner): Unit = {
    import sr.spark.implicits._
    val graph: Graph[User, String] = readData(sr)

    /* CanonicalGraph
        - There are no self edges
        - All edges are oriented (src is greater than dst)
        - There are no duplicate edges
     */
    println("CanonicalGraph edges:")
    val canonicalGraph: Graph[User, Int] =
      graph.mapEdges(e => 1).removeSelfEdges.convertToCanonicalEdges(_ + _)
    canonicalGraph.edges.toDF.sort($"srcId", $"dstId").show(numRows = 100)

    /* TriangleCount
    Compute the number of triangles passing through each vertex.
    The algorithm is relatively straightforward and can be computed in three steps:
    - Compute the set of neighbors for each vertex
    - For each edge compute the intersection of the sets and send the count to both vertices.
    - Compute the sum at each vertex and divide by two since each triangle is counted twice.
     */
    val neighbors: VertexRDD[Set[VertexId]] =
      canonicalGraph.aggregateMessages[Set[VertexId]](
        sendMsg = { (ec: EdgeContext[User, Int, Set[VertexId]]) =>
          ec.sendToDst(msg = Set(ec.srcId))
          ec.sendToSrc(msg = Set(ec.dstId))
        },
        mergeMsg = _ ++ _
      )
    println("Neighbors:")
    neighbors.toDF("id", "attr").sort($"id").show

    val graph1 = Graph(vertices = neighbors, edges = canonicalGraph.edges)

    val graph2: Graph[Set[VertexId], Set[VertexId]] = graph1.mapTriplets(
      map = tr => tr.srcAttr & tr.dstAttr
    )
    println("Intersection of neighbors:")
    graph2.edges.toDF.sort($"srcId", $"dstId").show(numRows = 100)

    val allTriangles: RDD[(VertexId, VertexId, VertexId)] =
      graph2.edges
        .filter(_.attr.nonEmpty)
        .flatMap { edge =>
          edge.attr.map { vid =>
            mkCanonicalTriangle(vid = vid, neighbours = (edge.srcId, edge.dstId))
          }.toSeq
        }
        .distinct()
    println("allTriangles:")
    allTriangles
      .toDF("vid1", "vid2", "vid3")
      .sort($"vid1", $"vid2", $"vid3")
      .show(numRows = 100)

    val triangleNeighbors: VertexRDD[Set[(VertexId, VertexId)]] =
      graph2.aggregateMessages[Set[(VertexId, VertexId)]](
        sendMsg = {
          (ec: EdgeContext[
            Set[VertexId],
            Set[VertexId],
            Set[(VertexId, VertexId)]
          ]) =>
            ec.sendToDst(msg = mkTriangleNeighborsMessage(ec.srcId, ec.attr))
            ec.sendToSrc(msg = mkTriangleNeighborsMessage(ec.dstId, ec.attr))
        },
        mergeMsg = _ ++ _
      )
    println("Triangle Neighbors:")
    triangleNeighbors.toDF("id", "attr").sort($"id").show(truncate = false)

    val triangleCounts: VertexRDD[Int] = triangleNeighbors.mapValues(_.size)
    println("triangleCounts:")
    triangleCounts.toDF("id", "attr").sort($"id").show(truncate = false)
    val totalTriangleCounts = triangleCounts.map(_._2).reduce(_ + _) / 3
    println("totalTriangleCounts = " + totalTriangleCounts)

  }

  def mkTriangleNeighborsMessage(
      vid: VertexId,
      attr: Set[VertexId]
  ): Set[(VertexId, VertexId)] = {
    attr.map { vid2 => if (vid <= vid2) (vid, vid2) else (vid2, vid) }
  }
  def mkCanonicalTriangle(
      vid: VertexId,
      neighbours: (VertexId, VertexId)
  ): (VertexId, VertexId, VertexId) = {
    val vid1 :: vid2 :: vid3 :: Nil = Seq(vid, neighbours._1, neighbours._2).sorted
    (vid1, vid2, vid3)
  }

  def run2(sr: SparkRunner): Unit = {
    import sr.spark.implicits._
    val graph: Graph[User, String] = readData(sr)
    val users: VertexRDD[User]     = graph.vertices

    // triangleCounts
    val triangleCounts: Graph[Int, String] = graph.triangleCount
    println("\ntriangleCounts:")
    triangleCounts.vertices.toDS.show
    triangleCounts.edges.toDS.show
    users
      .join(other = triangleCounts.vertices)
      .map { case (vid, (User(name, occupation), k)) =>
        (vid, name, occupation, k)
      }
      .toDS
      .show
    val triangleEdges: RDD[EdgeTriplet[Int, String]] =
      triangleCounts.triplets.filter { tr => tr.srcAttr > 0 && tr.dstAttr > 0 }
    triangleEdges.map(_.toTuple).toDS.show(truncate = false, numRows = 100)

    triangleCounts
      .subgraph(
        epred = tr => tr.srcAttr > 0 && tr.dstAttr > 0,
        vpred = (_, d) => d > 0
      )
      .triplets
      .map(_.toTuple)
      .toDS
      .show(numRows = 100)

  }

  def run1(sr: SparkRunner): Unit = {
    import sr.spark.implicits._
    val graph: Graph[User, String] = readData(sr)

    // EdgeTriplet
    val triplets: RDD[EdgeTriplet[User, String]] = graph.triplets
    triplets.map(_.toTuple).toDS.show(truncate = false, numRows = 100)
    val cntSameNameStart: Long = triplets.filter { tr =>
      tr.srcAttr.name.charAt(0) == tr.dstAttr.name.charAt(0)
    }.count
    println("cntSameNameStart = " + cntSameNameStart)

    // aggregateMessages, compute the in-degree of each vertex
    type Message = Int
    val aggregatedMessages: VertexRDD[Message] = graph.aggregateMessages[Message](
      sendMsg  = (x: EdgeContext[User, String, Message]) => x.sendToDst(msg = 1),
      mergeMsg = (m1: Message, m2: Message) => m1 + m2
    )
    val aggRDD: RDD[(Long, Int)] = aggregatedMessages.map { case (vid, msg) =>
      vid.toLong -> msg.toInt
    }
    aggRDD.toDF.show
  }

  def readData(sr: SparkRunner): Graph[User, String] = {
    import sr.spark.implicits._
    val dataPath =
      System.getProperty("user.dir") +
        "/x-graphx/data/packt/BigDataAnalytics/Chapter10-GraphX/"

    val usersPath = dataPath + "users.txt"
    //println(usersPath)
    val users: RDD[(Long, User)] =
      sr.spark.sparkContext.textFile(usersPath).map { line =>
        val fields = line.split(",")
        fields(0).toLong -> User(name = fields(1), occupation = fields(2))
      }

    val friendsPath = dataPath + "friends.txt"
    //println(friendsPath)
    val friends: RDD[Edge[String]] =
      sr.spark.sparkContext.textFile(friendsPath).map { line =>
        val fields = line.split(",")
        Edge(srcId = fields(0).toLong, dstId = fields(1).toLong, attr = "friend")
      }

    val graph: Graph[User, String] = Graph(vertices = users, edges = friends)
    println("vertices:")
    graph.vertices.toDF("id", "attr").sort($"id").show
    println("edges:")
    graph.edges.toDF.sort($"srcId", $"dstId").show(numRows = 100)
    graph
  }
}

/*
    a
    |\
    b-c
    |\|\
    d-e-f

1,a
2,b
3,c
4,d
5,e
6,f

a,b
a,c
b,c
b,d
b,e
c,e
c,f
d,e
e,f

 */
