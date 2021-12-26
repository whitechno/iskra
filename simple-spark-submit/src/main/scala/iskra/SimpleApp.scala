package iskra

import org.apache.spark.sql.SparkSession

/*
sbt> simple-spark-submit / runMain iskra.SimpleApp local[*]

 * This doesn't work if we need libraries not provided by Spark:
$ ~/dev/spark-bin/spark-3.0.1-bin-hadoop2.7/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit_2.12-0.1.1.jar

 * If we need libraries not provided by Spark, like com.typesafe.config,
 * then we need to generate assembly JAR with those libraries included.
 * Spark-provided libraries have to be marked as '% "provided"':
sbt> simple-spark-submit / assembly
$ jar tvf simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
$ ~/dev/apache-github/spark/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar

 * for simple-spark-databricks project:
sbt> simple-spark-databricks / assembly
$ jar tvf simple-spark-databricks/target/scala-2.12/simple-spark-databricks-assembly_2.12-0.1.1.jar
This doesn't work because of
"Exception in thread "main" java.lang.NoClassDefFoundError: com/typesafe/config/ConfigFactory"
$ ~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/spark-submit \
  --class "iskra.SimpleApp" \
  simple-spark-databricks/target/scala-2.12/simple-spark-databricks-assembly_2.12-0.1.1.jar

 * just to check that JAR's content is the same:
$ jar tvf simple-spark-provided/target/scala-2.12/simple-spark-provided-assembly_2.12-0.1.1.jar
 */
object SimpleApp {
  def main(args: Array[String]): Unit = {
    var builder           = SparkSession.builder().appName("Simple Application")
    var stopSparkWhenDone = false
    if (args.length > 0) {
      args.zipWithIndex.foreach(println)
      val master = args(0)
      builder           = builder.master(master)
      stopSparkWhenDone = true
    } else builder = builder.master("local")
    val spark = builder.getOrCreate()

    // to check if com.typesafe.config is available ar run time:
    val dir = com.typesafe.config.ConfigFactory.load().getString("user.dir")
    println(s"\n >>> dir = $dir \n")

    val logFile = dir + "/README.md"
    // "/Users/owhite/dev/apache-github/spark/README.md"

    val logData = spark.read.textFile(logFile).cache()
    val numAs   = logData.filter(line => line.contains("a")).count()
    val numBs   = logData.filter(line => line.contains("b")).count()
    println(s"\n >>> Lines with a: $numAs, Lines with b: $numBs \n")

    if (stopSparkWhenDone) spark.stop()
  }

}
