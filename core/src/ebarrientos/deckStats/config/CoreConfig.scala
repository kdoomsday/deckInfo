package ebarrientos.deckStats.config

import scala.concurrent.duration.FiniteDuration

case class RequestConfig(timeout: FiniteDuration, retryTime: FiniteDuration)
case class Paths(initScripts: String, xmlCards: String)

case class CoreConfig(
    dbConnectionUrl: String,
    dbDriver: String,
    dbThreads: Int,
    parallelMax: Int,
    requestConfig: RequestConfig,
    paths: Paths,
    port: Int
)
