package iskra.runner

case class RunnerInputSparkConfig(
    master: Option[String]     = None,
    appName: Option[String]    = None,
    logErrorLevelOnly: Boolean = true,
    sparkStopWhenDone: Boolean = false
)
