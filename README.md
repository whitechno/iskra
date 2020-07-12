Spark eXperiments
=================

Spark official
--------------

- [Building Spark](
https://spark.apache.org/docs/latest/building-spark.html
)

- [Useful Developer Tools](
https://spark.apache.org/developer-tools.html
)

- [Download Apache Spark](
https://spark.apache.org/downloads.html
)

[Spark Overview](
https://spark.apache.org/docs/3.0.0/
)

Downloaded pre-built Spark packages:
- ~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/  
  Spark 3.0.0 (git revision 3fdfce3120) built for Hadoop 2.7.4  
  Build flags:  
  -B -Pmesos -Pyarn -Pkubernetes -Psparkr -Pscala-2.12 -Phadoop-2.7  
  -Phive -Phive-thriftserver -DzincPort=3036

```
~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  simple-project/target/scala-2.12/simple-project_2.12-0.1.0-SNAPSHOT.jar
```

Latest Spark built from source using Maven:
- ~/dev/apache-github/spark/bin/

```
~/dev/apache-github/spark/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  simple-project/target/scala-2.12/simple-project_2.12-0.1.0-SNAPSHOT.jar
```


Spark submit, provided dependencies and assembly packages
---------------------------------------------------------

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

Maven
-----
- [Maven Releases History](
https://maven.apache.org/docs/history.html
)  
  - 3.6.3 - 2019-11-25
  - 3.6.2 - 2019-08-27
  - 3.6.1 - 2019-04-04
  - 3.6.0 - 2018-10-24


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