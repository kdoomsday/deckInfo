package ebarrientos.deckStats.config

import pureconfig._

case class CoreConfig(
    dbConnectionUrl: String,
    dbDriver: String,
    dbThreads: Int,
    parallelMax: Int
)
