package app.configuration.common

import app.properties.HBaseProperties
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.dwp.dataworks.logging.DataworksLogger

@Configuration
class HbaseConfiguration(private val properties: HBaseProperties) {

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
                set(HConstants.ZOOKEEPER_QUORUM, properties.zookeeperQuorum)
                setInt("hbase.zookeeper.port", 2181)
                setInt(HConstants.HBASE_RPC_WRITE_TIMEOUT_KEY, properties.rpcTimeoutMs)
                setInt(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, properties.clientTimeoutMs)
            }

    companion object {
        val logger = DataworksLogger.getLogger(HbaseConfiguration::class.java.toString())
    }
}
