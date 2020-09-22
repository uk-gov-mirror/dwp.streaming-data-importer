package app.configuration

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.dwp.dataworks.logging.DataworksLogger

@Configuration
@ConfigurationProperties(prefix = "hbase")
class HbaseConfiguration(private val zookeeperQuorum: String = "hbase",
                         private val rpcTimeoutMs: Int = 1_800_000,
                         private val clientTimeoutMs: Int = 3_600_000) {
    @Bean
    fun hbaseConnection(): Connection =
            configuration().run {
                logger.info("Timeout configuration",
                        "rpc" to get(HConstants.HBASE_RPC_WRITE_TIMEOUT_KEY),
                        "client" to get(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT))
                ConnectionFactory.createConnection(this)
            }


    private fun configuration() =
            HBaseConfiguration.create().apply {
                set(HConstants.ZOOKEEPER_QUORUM, zookeeperQuorum)
                setInt("hbase.zookeeper.port", 2181)
                setInt(HConstants.HBASE_RPC_WRITE_TIMEOUT_KEY, rpcTimeoutMs)
                setInt(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, clientTimeoutMs.toInt())
            }

    companion object {
        val logger = DataworksLogger.getLogger(HbaseConfiguration::class.java.toString())
    }
}
