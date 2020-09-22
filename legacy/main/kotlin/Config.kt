import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HConstants
import java.io.File
import java.time.Duration
import java.util.*
import java.util.regex.Pattern

fun getEnv(envVar: String): String? {
    val value = System.getenv(envVar)
    return if (value.isNullOrEmpty()) null else value
}

fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

object Config {
    const val schemaFileProperty = "schema.location"
    const val mainSchemaFile = "message.schema.json"
    const val equalitySchemaFile = "equality_message.schema.json"
    const val dataworksRegion = "eu-west-2"
    object Shovel {
        val reportFrequency = getEnv("K2HB_KAFKA_REPORT_FREQUENCY")?.toInt() ?: 100
    }

    object Validator {
        var properties = Properties().apply {
            put(schemaFileProperty, getEnv("K2HB_VALIDATOR_SCHEMA") ?: mainSchemaFile)
        }
    }

    object Hbase {
        val config = Configuration().apply {
            set(HConstants.ZOOKEEPER_ZNODE_PARENT, getEnv("K2HB_HBASE_ZOOKEEPER_PARENT") ?: "/hbase")
            set(HConstants.ZOOKEEPER_QUORUM, getEnv("K2HB_HBASE_ZOOKEEPER_QUORUM") ?: "zookeeper")
            setInt("hbase.zookeeper.port", getEnv("K2HB_HBASE_ZOOKEEPER_PORT")?.toIntOrNull() ?: 2181)
            set(HConstants.HBASE_RPC_TIMEOUT_KEY, getEnv("K2HB_HBASE_RPC_TIMEOUT_MILLISECONDS") ?: "1200000")
            set(HConstants.HBASE_CLIENT_OPERATION_TIMEOUT, getEnv("K2HB_HBASE_OPERATION_TIMEOUT_MILLISECONDS") ?: "1800000")
            set(HConstants.HBASE_CLIENT_PAUSE, getEnv("K2HB_HBASE_PAUSE_MILLISECONDS") ?: "50")
            set(HConstants.HBASE_CLIENT_RETRIES_NUMBER, getEnv("K2HB_HBASE_RETRIES") ?: "50")
            set("hbase.client.keyvalue.maxsize", getEnv("K2HB_HBASE_KEYVALUE_MAX_SIZE") ?: "0")
        }

        val columnFamily = getEnv("K2HB_HBASE_COLUMN_FAMILY") ?: "cf"
        val columnQualifier = getEnv("K2HB_HBASE_COLUMN_QUALIFIER") ?: "record"
        val retryMaxAttempts: Int = getEnv("K2HB_RETRY_MAX_ATTEMPTS")?.toInt() ?: 3
        val maxExistenceChecks: Int = getEnv("K2HB_MAX_EXISTENCE_CHECKS")?.toInt() ?: 3
        val checkExistence: Boolean = getEnv("K2HB_CHECK_EXISTENCE")?.toBoolean() ?: true
        val retryInitialBackoff: Long = getEnv("K2HB_RETRY_INITIAL_BACKOFF")?.toLong() ?: 10000
        val retryBackoffMultiplier: Long = getEnv("K2HB_RETRY_BACKOFF_MULTIPLIER")?.toLong() ?: 2
        val regionReplication: Int = getEnv("K2HB_HBASE_REGION_REPLICATION")?.toInt() ?: 3
        val logKeys: Boolean = getEnv("K2HB_HBASE_LOG_KEYS")?.toBoolean() ?: true
        var DEFAULT_QUALIFIED_TABLE_PATTERN = """^\w+\.([-\w]+)\.([-.\w]+)$"""
        var qualifiedTablePattern = getEnv("K2HB_QUALIFIED_TABLE_PATTERN") ?: DEFAULT_QUALIFIED_TABLE_PATTERN
    }


    object MetadataStore {
        val writeToMetadataStore = (getEnv("K2HB_WRITE_TO_METADATA_STORE") ?: "true").toBoolean()
        val metadataStoreTable = getEnv("K2HB_METADATA_STORE_TABLE") ?: "ucfs"

        private val useAwsSecretsString = getEnv("K2HB_USE_AWS_SECRETS") ?: "true"

        val isUsingAWS = useAwsSecretsString == "true"

        val properties = Properties().apply {
            put("user", getEnv("K2HB_RDS_USERNAME") ?: "k2hbwriter")
            put("rds.password.secret.name", getEnv("K2HB_RDS_PASSWORD_SECRET_NAME") ?: "password")
            put("database", getEnv("K2HB_RDS_DATABASE_NAME") ?: "metadatastore")
            put("rds.endpoint", getEnv("K2HB_RDS_ENDPOINT") ?: "127.0.0.1")
            put("rds.port", getEnv("K2HB_RDS_PORT") ?: "3306")
            put("use.aws.secrets", getEnv("K2HB_USE_AWS_SECRETS") ?: "true")

            if (isUsingAWS) {
                put("ssl_ca_path", getEnv("K2HB_RDS_CA_CERT_PATH") ?: "/certs/AmazonRootCA1.pem")
                put("ssl_ca", readFile(getProperty("ssl_ca_path")))
                put("ssl_verify_cert", true)
            }
        }
    }

    object SecretManager {
        val properties = Properties().apply {
            put("region", getEnv("SECRET_MANAGER_REGION") ?: dataworksRegion)
        }
    }

    object AwsS3 {
        val maxConnections: Int = (getEnv("K2HB_AWS_S3_MAX_CONNECTIONS") ?: "1000").toInt()
        val useLocalStack = (getEnv("K2HB_AWS_S3_USE_LOCALSTACK") ?: "false").toBoolean()
        val region = getEnv("K2HB_AWS_S3_REGION") ?: dataworksRegion
        val archiveBucket = getEnv("K2HB_AWS_S3_ARCHIVE_BUCKET") ?: "ucarchive"
        val archiveDirectory = getEnv("K2HB_AWS_S3_ARCHIVE_DIRECTORY") ?: "ucdata_main"
        val parallelPuts = (getEnv("K2HB_AWS_S3_PARALLEL_PUTS") ?: "false").toBoolean()
        val batchPuts = (getEnv("K2HB_AWS_S3_BATCH_PUTS") ?: "false").toBoolean()

        const val localstackServiceEndPoint = "http://aws-s3:4566/"
        const val localstackAccessKey = "AWS_ACCESS_KEY_ID"
        const val localstackSecretKey = "AWS_SECRET_ACCESS_KEY"
    }
}
