package ebarrientos.deckStats.config

import scala.concurrent.duration.FiniteDuration
import pureconfig.*
import pureconfig.generic.derivation.default.*

case class RequestConfig(timeout: FiniteDuration, retryTime: FiniteDuration, maxRetries: Int) derives ConfigReader
case class Paths(initScripts: String, xmlCards: String) derives ConfigReader

case class CoreConfig(
    dbConnectionUrl: String,
    dbDriver: String,
    dbThreads: Int,
    parallelMax: Int,
    requestConfig: RequestConfig,
    paths: Paths,
    port: Int
) derives ConfigReader
