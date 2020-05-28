commit 721cba540292d8d76102b18922dabe2a7d918dc5  
Author: Holden Karau <hkarau@apple.com>  
Date:   Fri May 22 18:19:41 2020 -0700

    [SPARK-31791][CORE][TEST] Improve cache block migration test reliability
    
    ### What changes were proposed in this pull request?
    
    Increase the timeout and register the listener earlier to avoid any race condition of the job starting before the listener is registered.
    
    ### Why are the changes needed?
    
    The test is currently semi-flaky.
    
    ### Does this PR introduce _any_ user-facing change?
    
    No
    
    ### How was this patch tested?
    I'm currently running the following bash script on my dev machine to verify the flakiness decreases. It has gotten to 356 iterations without any test failures so I believe issue is fixed.
    
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
    
    Closes #28614 from holdenk/SPARK-31791-improve-cache-block-migration-test-reliability.
    
    Authored-by: Holden Karau <hkarau@apple.com>
    Signed-off-by: Holden Karau <hkarau@apple.com>
