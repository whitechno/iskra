Spark eXperiments
=================

Spark official resources
------------------------

- [Download Apache Spark](https://spark.apache.org/downloads.html)
- [github.com/apache/spark](https://github.com/apache/spark)
- [Building Spark](https://spark.apache.org/docs/latest/building-spark.html)
- [Useful Developer Tools](https://spark.apache.org/developer-tools.html)

Project notes: running with various versions of Spark, Scala and Log4j
----------------------------------------------------------------------
To compile/test/package/assembly for all supportedScalaVersions (2.12 and 2.13), run:

```text
sbt> clean;+test:compile;+test;+assemblies;+package
```

### Run with the latest SNAPSHOT version of Spark and modified `log4j2.properties`

Latest Spark `3.4.0-SNAPSHOT` built from source using Maven:

```
$ cd $DEV/apache-github/spark/
$ ./build/mvn -DskipTests clean package
```

Modify `log4j2.properties` and execute `spark-submit`:

```
$ cp $DEV/apache-github/spark/conf/log4j2.properties.template \
     $DEV/apache-github/spark/conf/log4j2.properties 
and change "rootLogger.level = info" to "rootLogger.level = error".
$ $DEV/apache-github/spark/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

### Execute `spark-submit` using Spark's default log4j profile

***No control over Log4j***

`org/apache/spark/log4j-defaults.properties` for Log4j 1.2 in
[Spark 3.2.1 Logging](
https://github.com/apache/spark/blob/v3.2.1/core/src/main/scala/org/apache/spark/internal/Logging.scala#L132
)

```
$ $DEV/spark-bin/spark-3.2.1-bin-hadoop2.7/bin/spark-submit \
  --master local[4] \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

`org/apache/spark/log4j2-defaults.properties` for Log4j 2.0 in
[Spark 3.3.0 Logging](
https://github.com/apache/spark/blob/v3.3.0/core/src/main/scala/org/apache/spark/internal/Logging.scala#L136
)

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

Use `-Dlog4j.configuration` with Log4j 1.2:

```
$ $DEV/spark-bin/spark-3.2.1-bin-hadoop2.7/bin/spark-submit \
  --master local[4] \
  --driver-java-options "-Dlog4j.configuration=file:$DEV/spark-bin/conf/log4j.properties" \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.12/simple-spark-submit-assembly_2.12-0.1.1.jar
```

Use `-Dlog4j.configurationFile` with Log4j 2.0:

```
$ $DEV/spark-bin/spark-3.3.0-bin-hadoop3-scala2.13/bin/spark-submit \
  --master local[4] \
  --driver-java-options "-Dlog4j.configurationFile=file:$DEV/spark-bin/conf/log4j2.properties" \
  --class "iskra.SimpleApp" \
  simple-spark-submit/target/scala-2.13/simple-spark-submit-assembly_2.13-0.1.1.jar
```

### Create `conf` dir with `log4j.properties` and `log4j2.properties`

***Can be used with overridden `SPARK_CONF_DIR` environment variable.***

```
$ cp $DEV/spark-bin/spark-3.2.1-bin-hadoop3.2/conf/log4j.properties.template \
     $DEV/spark-bin/conf/log4j.properties
and change "log4j.rootCategory=INFO, console" to "log4j.rootCategory=ERROR, console".

$ cp $DEV/spark-bin/spark-3.3.0-bin-hadoop2/conf/log4j2.properties.template \
     $DEV/spark-bin/conf/log4j2.properties
and change "rootLogger.level = info" to "rootLogger.level = error".
```

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
and change "log4j.rootCategory=INFO, console" to "log4j.rootCategory=ERROR, console".
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
    - 3.3.0 - Jun 09, 2022 (first version with log4j 2.0)
- 3.2 both Scala 2.12 (Hadoop 2.7 and 3.3) and Scala 2.13 (Hadoop 3.3)
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

Log4j and Spark
---------------

- [how-to-stop-info-messages-displaying-on-spark-console](
  https://stackoverflow.com/questions/27781187/how-to-stop-info-messages-displaying-on-spark-console
  )

- [Spark Troubleshooting guide: Debugging Spark Applications: How to pass log4j.properties from executor and driver](
  https://support.datafabric.hpe.com/s/article/Spark-Troubleshooting-guide-Debugging-Spark-Applications-How-to-pass-log4j-properties-from-executor-and-driver?language=en_US
  )

Spark submit, provided dependencies and assembly packages
---------------------------------------------------------

- [sbt-how-to-set-transitive-dependencies-of-a-dependency-to-provided-later](
  https://stackoverflow.com/questions/34015452/sbt-how-to-set-transitive-dependencies-of-a-dependency-to-provided-later
  )

- [sbt-spark-package](
  https://github.com/databricks/sbt-spark-package
  )

- [How to add “provided” dependencies back to run/test tasks' classpath?](
  https://stackoverflow.com/questions/18838944/how-to-add-provided-dependencies-back-to-run-test-tasks-classpath
  )

- [Understanding build.sbt with sbt-spark-package plugin](
  https://stackoverflow.com/questions/54796866/understanding-build-sbt-with-sbt-spark-package-plugin
  )

- [Introduction to SBT for Spark Programmers](
  https://mungingdata.com/apache-spark/introduction-to-sbt/
  ) March 2019

- [Creating a Spark Project with SBT, IntelliJ, sbt-spark-package, and friends](
  https://medium.com/@mrpowers/creating-a-spark-project-with-sbt-intellij-sbt-spark-package-and-friends-cc9108751c28
  ) Sep 2017

- [Setting up a Spark machine learning project with Scala, sbt and MLlib](
  https://medium.com/@pedrodc/setting-up-a-spark-machine-learning-project-with-scala-sbt-and-mllib-831c329907ea
  ) Jan 2019

Other resources
---------------

- [spark-daria](https://github.com/MrPowers/spark-daria)

- [Scala and Spark for Big Data Analytics](
  https://www.packtpub.com/big-data-and-business-intelligence/scala-and-spark-big-data-analytics
  )  
  Md. Rezaul Karim, Sridhar Alla  
  July 24, 2017  
  898 pages

- [Mastering Apache Spark 2.x - Second Edition](
  https://www.packtpub.com/big-data-and-business-intelligence/mastering-apache-spark-2x-second-edition
  )  
  Romeo Kienzler  
  July 25, 2017  
  354 pages
