package app.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "hbase")
data class HBaseProperties(var zookeeperQuorum: String = "hbase",
                           var zookeeperPort: Int = 2181,
                           var rpcTimeoutMs: Int = 1_800_000,
                           var clientTimeoutMs: Int = 3_600_000)
