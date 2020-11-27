commit 433ae9064f55b8adb27b561e1ff17c32f0bf3465  
Author: yangjie01 <yangjie01@baidu.com>  
Date:   Fri Nov 27 15:47:39 2020 +0900

    [SPARK-33566][CORE][SQL][SS][PYTHON] Make unescapedQuoteHandling option 
    configurable when read CSV
    
    ### What changes were proposed in this pull request?
    There are some differences between Spark CSV, opencsv and commons-csv, 
    the typical case are described in SPARK-33566, When there are both unescaped 
    quotes and unescaped qualifier in value,  the results of parsing are different.
    
    The reason for the difference is Spark use `STOP_AT_DELIMITER` as default 
    `UnescapedQuoteHandling` to build `CsvParser` and it not configurable.
    
    On the other hand, opencsv and commons-csv use the parsing mechanism similar to 
    `STOP_AT_CLOSING_QUOTE ` by default.
    
    So this pr make `unescapedQuoteHandling` option configurable to get the same 
    parsing result as opencsv and commons-csv.
    
    ### Why are the changes needed?
    Make unescapedQuoteHandling option configurable when read CSV to make parsing 
    more flexible。
    
    ### Does this PR introduce _any_ user-facing change?
    No
    
    ### How was this patch tested?
    
    - Pass the Jenkins or GitHub Action
    
    - Add a new case similar to that described in SPARK-33566
    
    Closes #30518 from LuciferYang/SPARK-33566.
    
    Authored-by: yangjie01 <yangjie01@baidu.com>
    Signed-off-by: HyukjinKwon <gurwls223@apache.org>


commit 721cba540292d8d76102b18922dabe2a7d918dc5  
Author: Holden Karau `hkarau@apple.com`  
Date:   Fri May 22 18:19:41 2020 -0700

    [SPARK-31791][CORE][TEST] Improve cache block migration test reliability
    
    ### What changes were proposed in this pull request?
    
    Increase the timeout and register the listener earlier to avoid any race condition 
    of the job starting before the listener is registered.
    
    ### Why are the changes needed?
    
    The test is currently semi-flaky.
    
    ### Does this PR introduce _any_ user-facing change?
    
    No
    
    ### How was this patch tested?
    I'm currently running the following bash script on my dev machine to verify the 
    flakiness decreases. It has gotten to 356 iterations without any test failures so 
    I believe issue is fixed.
    
    ```
    set -ex
    ./build/sbt clean compile package
    ((failures=0))
    for (( i=0;i<1000;++i )); do
      echo "Run $i"
      ((failed=0))
      ./build/sbt "core/testOnly org.apache.spark.scheduler.WorkerDecommissionSuite" || ((failed=1))
      echo "Resulted in $failed"
      ((failures=failures+failed))
      echo "Current status is failures: $failures out of $i runs"
    done
    ```
    
    Closes #28614 from 
    holdenk/SPARK-31791-improve-cache-block-migration-test-reliability.
    
    Authored-by: Holden Karau <hkarau@apple.com>
    Signed-off-by: Holden Karau <hkarau@apple.com>
