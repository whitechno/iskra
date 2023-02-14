Spark eXperiments
=================
```text
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 3.4.0-SNAPSHOT
      /_/
```

<!-- TOC -->
* [Spark eXperiments](#spark-experiments)
  * [Spark official resources](#spark-official-resources)
  * [Build and test Spark](#build-and-test-spark)
  * [Project notes: running with various versions of Spark, Scala and Log4j](#project-notes--running-with-various-versions-of-spark-scala-and-log4j)
    * [Run with the latest SNAPSHOT version of Spark and modified `log4j2.properties`](#run-with-the-latest-snapshot-version-of-spark-and-modified-log4j2properties)
    * [Execute `spark-submit` using Spark's default log4j profile](#execute-spark-submit-using-sparks-default-log4j-profile)
    * [Execute `spark-submit` with `--driver-java-options`](#execute-spark-submit-with---driver-java-options)
    * [Create `conf` dir with `log4j.properties` and `log4j2.properties`](#create-conf-dir-with-log4jproperties-and-log4j2properties)
    * [Execute `spark-submit` with overridden `SPARK_CONF_DIR` environment variable](#execute-spark-submit-with-overridden-sparkconfdir-environment-variable)
    * [Execute `spark-submit` with modified `log4j.properties` in its default `conf` location](#execute-spark-submit-with-modified-log4jproperties-in-its-default-conf-location)
  * [Spark releases](#spark-releases)
  * [Downloaded pre-built Spark packages](#downloaded-pre-built-spark-packages)
  * [Hadoop](#hadoop)
  * [Maven](#maven)
  * [Spark and scalatest](#spark-and-scalatest)
  * [Spark and Log4j](#spark-and-log4j)
  * [Spark submit, provided dependencies and assembly packages](#spark-submit-provided-dependencies-and-assembly-packages)
  * [Other resources](#other-resources)
<!-- TOC -->

Spark official resources
------------------------
- [Download Apache Spark](https://spark.apache.org/downloads.html)
- [github.com/apache/spark](https://github.com/apache/spark)
- [Building Spark](https://spark.apache.org/docs/latest/building-spark.html)
- [Useful Developer Tools](https://spark.apache.org/developer-tools.html)
- [Databricks Scala style](https://github.com/databricks/scala-style-guide)

Build and test Spark
--------------------
Latest Spark `3.4.0-SNAPSHOT` built from source using Maven:
```
$ cd $DEV/apache-github/spark/
$ ./build/mvn -DskipTests clean package
build Spark submodules using the mvn -pl option, like:
$ ./build/mvn -pl :spark-streaming_2.12 clean install
or using SBT:
$ ./build/sbt package
```

Testing with SBT
```
$ ./build/sbt
sbt> core/test
sbt> testOnly org.apache.spark.scheduler.DAGSchedulerSuite
sbt> testOnly *DAGSchedulerSuite
sbt> testOnly org.apache.spark.scheduler.*
sbt> testOnly *DAGSchedulerSuite -- -z "[SPARK-3353]"
$ build/sbt "core/testOnly *DAGSchedulerSuite -- -z SPARK-3353"
To see test logs:
$ cat core/target/unit-tests.log
```

Testing with Maven
```
To run individual Scala tests:
$ build/mvn \
-Dtest=none -DwildcardSuites=org.apache.spark.scheduler.DAGSchedulerSuite
To run individual Java tests:
$ build/mvn test \
-DwildcardSuites=none -Dtest=org.apache.spark.streaming.JavaAPISuite test
```

Project notes: running with various versions of Spark, Scala and Log4j
----------------------------------------------------------------------
To compile/test/package/assembly for all supportedScalaVersions (2.12 and 2.13), run:
```text
sbt> clean;+test:compile;+test;+assemblies;+package
```

### Run with the latest SNAPSHOT version of Spark and modified `log4j2.properties`

Modify `log4j2.properties`:
```
$ cp $DEV/apache-github/spark/conf/log4j2.properties.template \
     $DEV/apache-github/spark/conf/log4j2.properties 
and change "rootLogger.level = info" to "rootLogger.level = warn".
```

Execute `spark-submit`:
```
$ $DEV/apache-github/spark/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

### Execute `spark-submit` using Spark's default log4j profile

***No control over Log4j***

For Log4j 1.2, Spark's default log4j profile:
[org/apache/spark/log4j-defaults.properties](
https://github.com/apache/spark/blob/v3.2.1/core/src/main/resources/org/apache/spark/log4j-defaults.properties
) See [Spark 3.2.1 Logging](
https://github.com/apache/spark/blob/v3.2.1/core/src/main/scala/org/apache/spark/internal/Logging.scala#L132
) and [Utils.setLogLevel](
https://github.com/apache/spark/blob/v3.2.1/core/src/main/scala/org/apache/spark/util/Utils.scala#L2415
).
```
$ $DEV/spark-bin/spark-3.2.1-bin-hadoop2.7/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

For Log4j 2.0, Spark's default log4j profile:
[org/apache/spark/log4j2-defaults.properties](
https://github.com/apache/spark/blob/v3.3.0/core/src/main/resources/org/apache/spark/log4j2-defaults.properties
). See [Spark 3.3.0 Logging](
https://github.com/apache/spark/blob/v3.3.0/core/src/main/scala/org/apache/spark/internal/Logging.scala#L136
) and [Utils.setLogLevel](
https://github.com/apache/spark/blob/v3.3.0/core/src/main/scala/org/apache/spark/util/Utils.scala#L2462
).
```
$ $DEV/spark-bin/spark-3.3.0-bin-hadoop2/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

```
$ $DEV/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
```

### Execute `spark-submit` with `--driver-java-options`

***This is the FIRST preferred method of controlling Log4j.***

This works only in local client mode. In standalone and cluster mode additional
`spark-submit` settings are needed.

Use `-Dlog4j.configuration=file:$DEV/spark-bin/conf/log4j.properties` with Log4j 1.2.

Run locally:
```
$ $DEV/spark-bin/spark-3.2.1-bin-hadoop2.7/bin/spark-submit \
  --master local[4] \
  --driver-java-options \
"-Dlog4j.configuration=file:simple-spark-submit/spark-submit-conf/log4j.properties" \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

Use `-Dlog4j.configurationFile=file:$DEV/spark-bin/conf/log4j2.properties` with Log4j
2.0

Run locally:
```
$ $DEV/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/spark-submit \
  --master local[4] \
  --driver-java-options \
"-Dlog4j.configurationFile=file:simple-spark-submit/spark-submit-conf/log4j2.properties" \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
```

Run on a Spark standalone cluster in client deploy mode:
```
$ $DEV/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/spark-submit \
  --master spark://Olegs-MacBook-Pro.local:7077 \
  --driver-java-options \
"-Dlog4j.configurationFile=file:simple-spark-submit/spark-submit-conf/log4j2.properties" \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
```

Run on a Spark standalone cluster in cluster deploy mode:
```
$ $DEV/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/spark-submit \
  --master spark://Olegs-MacBook-Pro.local:7077 \
  --deploy-mode cluster \
  --conf "spark.driver.extraJavaOptions=\
-Dlog4j.configurationFile=file:$PWD/simple-spark-submit/spark-submit-conf/log4j2.properties" \
  --conf "spark.executor.extraJavaOptions=\
-Dlog4j.configurationFile=file:$PWD/simple-spark-submit/spark-submit-conf/log4j2.properties" \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
```

Note that the file needs to exist locally on all the nodes. To satisfy that
condition, you can either upload the file to the location available for the nodes
(like hdfs) or access it locally with driver if using deploy-mode client. Otherwise,
upload a custom `log4j.properties` using `spark-submit`, by adding it to
the `--files` list of files to be uploaded with the application. Something like this:
```
spark-submit \
    --master yarn \
    --deploy-mode cluster \
    --conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
    --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4j.properties" \
    --files "/absolute/path/to/your/log4j.properties" \
    --class com.github.atais.Main \
    "SparkApp.jar"
```
Note that files uploaded to spark-cluster with `--files` will be available at root
dir, so there is no need to add any path in `file:log4j.properties`.
Files listed in `--files` must be provided with absolute path!
`file:` prefix in configuration URI is mandatory.

### Create `conf` dir with `log4j.properties` and `log4j2.properties`

***Can be used with overridden `SPARK_CONF_DIR` environment variable.***

For Log4j 1.2:
```
$ cp $DEV/spark-bin/spark-3.2.1-bin-hadoop3.2/conf/log4j.properties.template \
     $DEV/spark-bin/conf/log4j.properties
```
and change "log4j.rootCategory=INFO, console" to "log4j.rootCategory=ERROR, console".

For Log4j 2.0:
```
$ cp $DEV/spark-bin/spark-3.3.0-bin-hadoop2/conf/log4j2.properties.template \
     $DEV/spark-bin/conf/log4j2.properties
```
and change "rootLogger.level = info" to "rootLogger.level = error".

### Execute `spark-submit` with overridden `SPARK_CONF_DIR` environment variable

***This is the SECOND preferred method of controlling Log4j.***

```
$ export SPARK_CONF_DIR=$DEV/spark-bin/conf
  $DEV/spark-bin/spark-3.2.1-bin-hadoop2.7/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
  unset SPARK_CONF_DIR
```

```
$ export SPARK_CONF_DIR=$DEV/spark-bin/conf 
  $DEV/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
  unset SPARK_CONF_DIR
```

### Execute `spark-submit` with modified `log4j.properties` in its default `conf` location

***This is the THIRD preferred method of controlling Log4j.***

```
$ cp $DEV/spark-bin/spark-3.2.1-bin-hadoop3.2-scala2.13/conf/log4j.properties.template \
     $DEV/spark-bin/spark-3.2.1-bin-hadoop3.2-scala2.13/conf/log4j.properties 
```
and change "log4j.rootCategory=INFO, console" to "log4j.rootCategory=ERROR, console".
```
$ $DEV/spark-bin/spark-3.2.1-bin-hadoop3.2-scala2.13/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
```

Spark releases
--------------
[github releases](https://github.com/apache/spark/releases)  
[Sonatype | Maven Central Repository](
https://search.maven.org/search?q=g:org.apache.spark)

- 3.3 both Scala 2.12 (Hadoop 2.7 and 3.3) and Scala 2.13 (Hadoop 3.3)
  - 3.3.1 - Oct 15, 2022
  - 3.3.0 - Jun 09, 2022 (first version with log4j 2.0)
- 3.2 both Scala 2.12 (Hadoop 2.7 and 3.3) and Scala 2.13 (Hadoop 3.3)
  - 3.2.3 - Nov 14, 2022
  - 3.2.2 - Jul 11, 2022
  - 3.2.1 - Jan 19, 2022 (last version with log4j 1.2)
  - 3.2.0 - Oct 06, 2021
- 3.1 Scala 2.12
  - 3.1.3 - Feb 06, 2022 (Hadoop 2.7 and 3.2)
  - 3.1.2 - May 23, 2021
  - 3.1.1 - Feb 21, 2021
  - 3.1.0 - Jan 05, 2021
- 3.0 Scala 2.12
  - 3.0.3 - Jun 14, 2021 (Hadoop 2.7 and 3.2)
  - 3.0.2 - Feb 19, 2021
  - 3.0.1 - Aug 27, 2020
  - 3.0.0 - Jun 05, 2020
- 2.4 Scala 2.11 (Hadoop 2.7.3)
  - 2.4.8 - May 09, 2021
  - 2.4.7 - Sep 07, 2020
  - 2.4.6 - May 29, 2020
  - 2.4.5 - Feb 02, 2020

Downloaded pre-built Spark packages
-----------------------------------
[Download Apache Spark](https://spark.apache.org/downloads.html)

- 3.3 (Scala 2.12 and 2.13)
  - 3.3.0 - Jun 16, 2022 (first version with log4j 2.0)
    - Hadoop 2.7.4 and Scala 2.12  
      `~/dev/spark-bin/spark-3.3.0-bin-hadoop2/bin/`
    - Hadoop 3.3.2 and Scala 2.12  
      `~/dev/spark-bin/spark-3.3.0-bin-hadoop3/bin/`
    - Hadoop 3.3.2 and Scala 2.13  
      `~/dev/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/`
- 3.2 (Scala 2.12 and 2.13)
  - 3.2.1 - Jan 26, 2022 (last version with log4j 1.2)
    - Hadoop 2.7.4 and Scala 2.12  
      `~/dev/spark-bin/spark-3.2.1-bin-hadoop2.7/bin/`
    - Hadoop 3.3.1 and Scala 2.12  
      `~/dev/spark-bin/spark-3.2.1-bin-hadoop3.2/bin/`
    - Hadoop 3.3.1 and Scala 2.13  
      `~/dev/spark-bin/spark-3.2.1-bin-hadoop3.2-scala2.13/bin/`
  - 3.2.0 - Oct 13, 2021
    - Hadoop 3.3.1 and Scala 2.12  
      `~/dev/spark-bin/spark-3.2.0-bin-hadoop3.2/bin/`
    - Hadoop 3.3.1 and Scala 2.13  
      `~/dev/spark-bin/spark-3.2.0-bin-hadoop3.2-scala2.13/bin/`
- 3.1 (Scala 2.12)
  - 3.1.3 - Feb 18, 2022
    - Hadoop 2.7.4  
      `~/dev/spark-bin/spark-3.1.3-bin-hadoop2.7/bin/`
    - Hadoop 3.2.0  
      `~/dev/spark-bin/spark-3.1.3-bin-hadoop3.2/bin/`
  - 3.1.2 - Jun 01, 2021 (Hadoop 3.2.0)  
    `~/dev/spark-bin/spark-3.1.2-bin-hadoop3.2/bin/`
  - 3.1.1 - Mar 02, 2021 (Hadoop 2.7.4)  
    `~/dev/spark-bin/spark-3.1.1-bin-hadoop2.7/bin/`
- 3.0 (Scala 2.12)
  - 3.0.2 - Feb 19, 2021  
    `~/dev/spark-bin/spark-3.0.2-bin-hadoop2.7/bin/`
  - 3.0.1  
    `~/dev/spark-bin/spark-3.0.1-bin-hadoop2.7/bin/`
  - 3.0.0 (Hadoop 2.7.4)  
    `~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/`
- 2.4 (Scala 2.11)
  - 2.4.8 - May 17, 2021 (Hadoop 2.7.3)  
    `~/dev/spark-bin/spark-2.4.8-bin-hadoop2.7/bin/`
  - 2.4.7 - Sep 12, 2020 (Hadoop 2.7.3)  
    `~/dev/spark-bin/spark-2.4.7-bin-hadoop2.7/bin/`

Hadoop
------
[Releases Archive](https://hadoop.apache.org/release.html)

- 3.3
  - 3.3.3 - May 17, 2022
  - 3.3.2 - Mar 03, 2021 (Spark 3.3.0)
  - 3.3.1 - Jun 15, 2021 (Spark 3.2.0)
  - 3.3.0 - Jul 14, 2020
- 3.2
  - 3.2.3 - Mar 28, 2022
  - 3.2.2 - Jan 09, 2021
  - 3.2.1 - Sep 22, 2019
  - 3.2.0 - Jan 16, 2019 (stable) (Spark 3.1.2)
- 3.1
  - 3.1.4 - Aug 03, 2020
  - 3.1.3 - Oct 21, 2019
  - 3.1.2 - Feb 06, 2019
  - 3.1.1 - Aug 08, 2018 (stable)
  - 3.1.0 - Apr 06, 2018
- 3.0
  - 3.0.3 - May 31, 2018
  - 3.0.2 - Apr 21, 2018
  - 3.0.1 - Mar 25, 2018
  - 3.0.0 - Dec 13, 2017
- 2.10
  - 2.10.2 - May 31, 2022
  - 2.10.1 - Sep 21, 2020
  - 2.10.0 - Oct 29, 2019 (stable)
- 2.9
  - 2.9.2 - Nov 19, 2018
  - 2.9.1 - May 03, 2018
  - 2.9.0 - Dec 17, 2017
- 2.8
  - 2.8.5 - Sep 15, 2018
  - 2.8.4 - May 15, 2018
  - 2.8.3 - Dec 12, 2017
  - 2.8.2 - Oct 24, 2017
  - 2.8.1 - Jun 08, 2017
  - 2.8.0 - Mar 22, 2017
- 2.7
  - 2.7.7 - May 31, 2018
  - 2.7.6 - Apr 16, 2018
  - 2.7.5 - Dec 14, 2017
  - 2.7.4 - Aug 04, 2017 (Spark 3.0.0)
  - 2.7.3 - Aug 26, 2016 (Spark 2.4)
  - 2.7.2 - Jan 25, 2016
  - 2.7.1 - Jul 06, 2015 (stable)
  - 2.7.0 - Apr 21, 2015

Maven
-----
[Maven Releases History](https://maven.apache.org/docs/history.html)

- 3.8.6 - 2022-06-06
- 3.8.5 - 2022-03-05
- 3.8.4 - 2021-11-14
- 3.8.3 - 2021-09-27
- 3.8.2 - 2021-08-04
- 3.8.1 - 2021-04-04
- 3.6.3 - 2019-11-25
- 3.6.2 - 2019-08-27
- 3.6.1 - 2019-04-04
- 3.6.0 - 2018-10-24

Spark and scalatest
-------------------
Spark's own codebase provides good examples and best practices for using scalatest to
do unit tests of Spark. In particular, the [spark-sql](
https://github.com/apache/spark/tree/v3.3.0/sql/core/src/test) has the following test
setup.

[SharedSparkSession and SharedSparkSessionBase](
https://github.com/apache/spark/blob/v3.3.0/sql/core/src/test/scala/org/apache/spark/sql/test/SharedSparkSession.scala
) Suites extending trait `SharedSparkSession` are sharing resources (e.g.
SparkSession) in their tests. That trait initializes the spark session in
its `beforeAll()` implementation. Helper trait `SharedSparkSessionBase` for SQL test
suites where all tests share a single `TestSparkSession`.

[TestSparkSession and TestSQLContext](
https://github.com/apache/spark/blob/v3.3.0/sql/core/src/test/scala/org/apache/spark/sql/test/TestSQLContext.scala
) The `TestSparkSession` to use for all tests in `SharedSparkSession` suite. By
default, the underlying `org.apache.spark.SparkContext` will be run in local mode
with the default test configurations.

Example of a test suite:
[class DatasetSuite extends QueryTest
with SharedSparkSession with AdaptiveSparkPlanHelper](
https://github.com/apache/spark/blob/v3.3.0/sql/core/src/test/scala/org/apache/spark/sql/DatasetSuite.scala
)

Spark and Log4j
---------------
- [Spark Configuration](
  https://spark.apache.org/docs/latest/configuration.html)
- [log4j manual](
  https://logging.apache.org/log4j/1.2/manual.html)
- [Class PatternLayout](
  https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html
  ) which explains patterns like `%-6r [%15.15t] %-5p %30.30c %x - %m%n`.
- In [How to stop INFO messages displaying on spark console?](
  https://stackoverflow.com/questions/27781187/how-to-stop-info-messages-displaying-on-spark-console/
  ) go to "All the methods collected with examples" answer. It contains very detailed
  instructions that **work**.
- [Spark Troubleshooting guide: Debugging Spark Applications: How to pass log4j.properties from executor and driver](
  https://support.datafabric.hpe.com/s/article/Spark-Troubleshooting-guide-Debugging-Spark-Applications-How-to-pass-log4j-properties-from-executor-and-driver?language=en_US)
- [spark-submit, how to specify log4j.properties](
  https://stackoverflow.com/questions/42230235/spark-submit-how-to-specify-log4j-properties)

Spark submit, provided dependencies and assembly packages
---------------------------------------------------------
- [sbt-how-to-set-transitive-dependencies-of-a-dependency-to-provided-later](
  https://stackoverflow.com/questions/34015452/sbt-how-to-set-transitive-dependencies-of-a-dependency-to-provided-later)
- [sbt-spark-package](
  https://github.com/databricks/sbt-spark-package)
- [How to add “provided” dependencies back to run/test tasks' classpath?](
  https://stackoverflow.com/questions/18838944/how-to-add-provided-dependencies-back-to-run-test-tasks-classpath)
- [Understanding build.sbt with sbt-spark-package plugin](
  https://stackoverflow.com/questions/54796866/understanding-build-sbt-with-sbt-spark-package-plugin)
- [Introduction to SBT for Spark Programmers](
  https://mungingdata.com/apache-spark/introduction-to-sbt/) March 2019
- [Creating a Spark Project with SBT, IntelliJ, sbt-spark-package, and friends](
  https://medium.com/@mrpowers/creating-a-spark-project-with-sbt-intellij-sbt-spark-package-and-friends-cc9108751c28)
  Sep 2017
- [Setting up a Spark machine learning project with Scala, sbt and MLlib](
  https://medium.com/@pedrodc/setting-up-a-spark-machine-learning-project-with-scala-sbt-and-mllib-831c329907ea)
  Jan 2019

Other resources
---------------
- [spark-daria](https://github.com/MrPowers/spark-daria)
- [Scala and Spark for Big Data Analytics](
  https://www.packtpub.com/big-data-and-business-intelligence/scala-and-spark-big-data-analytics
  ) Md. Rezaul Karim, Sridhar Alla, July 24, 2017, 898 pages.
- [Mastering Apache Spark 2.x - Second Edition](
  https://www.packtpub.com/big-data-and-business-intelligence/mastering-apache-spark-2x-second-edition
  ) Romeo Kienzler, July 25, 2017, 354 pages.
