package app.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "hbase")
data class HBaseProperties(val zookeeperQuorum: String = "hbase",
                           val rpcTimeoutMs: Int = 1_800_000,
                           val clientTimeoutMs: Int = 3_600_000)
