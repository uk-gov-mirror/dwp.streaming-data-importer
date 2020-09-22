
import org.apache.hadoop.hbase.*
import org.apache.hadoop.hbase.client.*
import org.apache.hadoop.hbase.io.TimeRange
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm
import uk.gov.dwp.dataworks.logging.DataworksLogger
import java.io.IOException
import kotlin.system.measureTimeMillis

open class HbaseClient(val connection: Connection, private val columnFamily: ByteArray,
                       private val columnQualifier: ByteArray, private val hbaseRegionReplication: Int): AutoCloseable {

    @Throws(IOException::class)
    open fun putList(tableName: String, payloads: List<HbasePayload>) {
        if (payloads.isNotEmpty()) {
            val timeTaken = measureTimeMillis {
                logger.info("Putting batch into table", "size" to "${payloads.size}", "table" to tableName)
                ensureTable(tableName)
                connection.getTable(TableName.valueOf(tableName)).use { table ->
                    table.put(payloads.map { payload ->
                        Put(payload.key).apply {
                            addColumn(columnFamily, columnQualifier, payload.version, payload.body)
                        }
                    })
                }
            }
            logger.info("Put batch into table", "time_taken" to "$timeTaken", "size" to "${payloads.size}", "table" to tableName)
        }
    }

    fun getCellAfterTimestamp(tableName: String, key: ByteArray, timestamp: Long): ByteArray? {
        connection.getTable(TableName.valueOf(tableName)).use { table ->
            val result = table.get(Get(key).apply {
                setTimeRange(timestamp, TimeRange.INITIAL_MAX_TIMESTAMP)
            })
            return result.getValue(columnFamily, columnQualifier)
        }
    }

    fun getCellBeforeTimestamp(tableName: String, key: ByteArray, timestamp: Long): ByteArray? {
        connection.getTable(TableName.valueOf(tableName)).use { table ->
            val result = table.get(Get(key).apply {
                setTimeRange(0, timestamp)
            })

            return result.getValue(columnFamily, columnQualifier)
        }
    }

    @Synchronized
    open fun ensureTable(tableName: String) {
        val dataTableName = TableName.valueOf(tableName)
        val namespace = dataTableName.namespaceAsString

        if (!namespaces.contains(namespace)) {
            try {
                logger.info("Creating namespace", "namespace" to namespace)
                connection.admin.createNamespace(NamespaceDescriptor.create(namespace).build())
            }
            catch (e: NamespaceExistException) {
                logger.info("Namespace already exists, probably created by another process", "namespace" to namespace)
            }
            finally {
                namespaces[namespace] = true
            }
        }

        if (!tables.contains(tableName)) {
            logger.info("Creating table", "table_name" to "$dataTableName")
            try {
                connection.admin.createTable(HTableDescriptor(dataTableName).apply {
                    addFamily(
                        HColumnDescriptor(columnFamily)
                            .apply {
                                maxVersions = Int.MAX_VALUE
                                minVersions = 1
                                compressionType = Algorithm.GZ
                                compactionCompressionType = Algorithm.GZ
                            })
                    regionReplication = hbaseRegionReplication
                })
            } catch (e: TableExistsException) {
                logger.info("Didn't create table, table already exists, probably created by another process", "table_name" to tableName)
            } finally {
                tables[tableName] = true
            }
        }
    }

    private val namespaces by lazy {
        val extantNamespaces = mutableMapOf<String, Boolean>()

        connection.admin.listNamespaceDescriptors()
            .forEach {
                extantNamespaces[it.name] = true
            }

        extantNamespaces
    }

    private val tables by lazy {
        val names = mutableMapOf<String, Boolean>()

        connection.admin.listTableNames().forEach {
            names[it.nameAsString] = true
        }

        names
    }

    companion object {
        fun connect(): HbaseClient {
            logger.info("Hbase connection configuration",
                    HConstants.ZOOKEEPER_ZNODE_PARENT to Config.Hbase.config.get(HConstants.ZOOKEEPER_ZNODE_PARENT),
                    HConstants.ZOOKEEPER_QUORUM to Config.Hbase.config.get(HConstants.ZOOKEEPER_QUORUM),
                    "hbase.zookeeper.port" to Config.Hbase.config.get("hbase.zookeeper.port")
            )

            return HbaseClient(ConnectionFactory.createConnection(HBaseConfiguration.create(Config.Hbase.config)),
                        Config.Hbase.columnFamily.toByteArray(),
                        Config.Hbase.columnQualifier.toByteArray(),
                        Config.Hbase.regionReplication)
        }

        private val logger = DataworksLogger.getLogger(HbaseClient::class.java.toString())
    }

    override fun close() = connection.close()
}
