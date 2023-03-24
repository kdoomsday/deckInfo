package ebarrientos.deckStats.config

import pureconfig._
import scala.concurrent.duration.FiniteDuration

case class RequestConfig(timeout: FiniteDuration)

case class CoreConfig(
    dbConnectionUrl: String,
    dbDriver: String,
    dbThreads: Int,
    parallelMax: Int,
    requestConfig: RequestConfig
)
