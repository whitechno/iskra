import org.apache.spark.sql.SparkSession
/*
> sbt package
$ ~/dev/apache-github/spark/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  simple-project/target/scala-2.12/simple-project_2.12-0.1.0-SNAPSHOT.jar
 */
// > simple-project / runMain SimpleApp local[4]
object SimpleApp {
  def main(args: Array[String]) {
    var builder = SparkSession.builder.appName("Simple Application")
    if (args.length > 0) {
      args.foreach(println)
      builder = builder.master("local")
    }
    builder = builder.master("local")
    val logFile =
      "/Users/owhite/dev/apache-github/spark/README.md" // Should be some file on your system
    val spark = builder.getOrCreate()

    val logData = spark.read.textFile(logFile).cache()
    val numAs   = logData.filter(line => line.contains("a")).count()
    val numBs   = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()

  }

}
