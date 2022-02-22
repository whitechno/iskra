Spark eXperiments
=================

Spark official resources
------------------------

- Spark releases  
  https://github.com/apache/spark/releases
    - 3.2 both Scala 2.12 (Hadoop 2.7 and 3.3) and Scala 2.13 (Hadoop 3.3)
        - 3.2.1 - Jan 19, 2022
        - 3.2.0 - Oct 06, 2021
    - 3.1
        - 3.1.3 - Feb 06, 2022 (Hadoop 2.7 and 3.2)
        - 3.1.2 - May 23, 2021
        - 3.1.1 - Feb 21, 2021
        - 3.1.0 - Jan 05, 2021
    - 3.0
        - 3.0.3 - Jun 14, 2021 (Hadoop 2.7 and 3.2)
        - 3.0.2 - Feb 19, 2021
        - 3.0.1 - Aug 27, 2020
        - 3.0.0 - Jun 05, 2020
    - 2.4
        - 2.4.8 - May 09, 2021
        - 2.4.7 - Sep 07, 2020
        - 2.4.6 - May 29, 2020
        - 2.4.5 - Feb 02, 2020

- [Download Apache Spark](
  https://spark.apache.org/downloads.html
  )

- [github.com/apache/spark](
  https://github.com/apache/spark
  )

- [Building Spark](
  https://spark.apache.org/docs/latest/building-spark.html
  )

- [Useful Developer Tools](
  https://spark.apache.org/developer-tools.html
  )

Project notes
-------------

Latest Spark `3.3.0-SNAPSHOT` built from source using Maven:

```
$ cd ~/dev/apache-github/spark/
$ ./build/mvn -DskipTests clean package

$ ~/dev/apache-github/spark/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  simple-project/target/scala-2.12/simple-project_2.12-0.1.0-SNAPSHOT.jar
```

Downloaded pre-built Spark packages:

- 3.2
    - 3.2.0 (Hadoop 3.3.1 and Scala 2.12) (Oct 13, 2021)  
      `~/dev/spark-bin/spark-3.2.0-bin-hadoop3.2/bin/`
    - 3.2.0 (Hadoop 3.3.1 and Scala 2.13) (Oct 13, 2021)  
      `~/dev/spark-bin/spark-3.2.0-bin-hadoop3.2-scala2.13/bin/`
- 3.1
    - 3.1.2 (Hadoop 3.2.0) (Jun 01, 2021)  
      `$ cd ~/dev/spark-bin/spark-3.1.2-bin-hadoop3.2/bin/`
    - 3.1.1 (Hadoop 2.7.4) (Mar 02, 2021)  
      `$ cd ~/dev/spark-bin/spark-3.1.1-bin-hadoop2.7/bin/`
- 3.0
    - 3.0.2 (Feb 19, 2021)  
      `$ cd ~/dev/spark-bin/spark-3.0.2-bin-hadoop2.7/bin/`
    - 3.0.1  
      `$ cd ~/dev/spark-bin/spark-3.0.1-bin-hadoop2.7/bin/`
    - 3.0.0 (Hadoop 2.7.4)
      ```
      $ cd ~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/  
        Spark 3.0.0 (git revision 3fdfce3120) built for Hadoop 2.7.4  
        Build flags:  
        -B -Pmesos -Pyarn -Pkubernetes -Psparkr -Pscala-2.12 -Phadoop-2.7  
        -Phive -Phive-thriftserver -DzincPort=3036
      
      $ ~/dev/spark-bin/spark-3.0.0-bin-hadoop2.7/bin/spark-submit \
        --class "SimpleApp" \
        --master local[4] \
        simple-project/target/scala-2.12/simple-project_2.12-0.1.0-SNAPSHOT.jar
      ```
- 2.4
    - 2.4.8 (Hadoop 2.7.3) (May 17, 2021)  
      `$ cd ~/dev/spark-bin/spark-2.4.8-bin-hadoop2.7/bin/`
    - 2.4.7 (Hadoop 2.7.3) (Sep 12, 2020)  
      `$ cd ~/dev/spark-bin/spark-2.4.7-bin-hadoop2.7/bin/`

Hadoop
------
[Releases Archive](https://hadoop.apache.org/release.html)

- 3.3
    - 3.3.1 - Jun 15, 2021 (Spark 3.2.0)
    - 3.3.0 - Jul 14, 2020
- 3.2
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

- [Maven Releases History](
  https://maven.apache.org/docs/history.html
  )
    - 3.8.4 - 2021-11-14
    - 3.8.3 - 2021-09-27
    - 3.8.2 - 2021-08-04
    - 3.8.1 - 2021-04-04
    - 3.6.3 - 2019-11-25
    - 3.6.2 - 2019-08-27
    - 3.6.1 - 2019-04-04
    - 3.6.0 - 2018-10-24

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
