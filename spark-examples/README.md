Running example programs
========================

```
$ cd $DEV/apache-github/spark/
$ ./build/sbt "examples/package"
To run examples, use `run-example`:
$ export RUN_EXAMPLE='./bin/run-example --driver-java-options 
  "-Dlog4j.configurationFile=file:conf/run-example-log4j2.properties"'
$ unset RUN_EXAMPLE
or `spark-submit`:
$ export SPARK_SUBMIT_EXAMPLE='./bin/spark-submit --driver-java-options 
  "-Dlog4j.configurationFile=file:conf/run-example-log4j2.properties"
  --master local[4]'
$ unset SPARK_SUBMIT_EXAMPLE

$ $RUN_EXAMPLE AccumulatorMetricsTest
$ $RUN_EXAMPLE BroadcastTest

$ $RUN_EXAMPLE GroupByTest

$ $RUN_EXAMPLE SparkPi
$ $SPARK_SUBMIT_EXAMPLE \
  --class "org.apache.spark.examples.SparkPi" \
  examples/target/scala-2.12/jars/spark-examples_2.12-3.4.0-SNAPSHOT.jar

--files FILES  Comma-separated list of files to be placed in the working
               directory of each executor. File paths of these files
               in executors can be accessed via SparkFiles.get(fileName).
$ $RUN_EXAMPLE \
--files "conf/run-example-log4j2.properties" \
SparkRemoteFileTest run-example-log4j2.properties

$ $RUN_EXAMPLE SparkTC

```
