
import kotlinx.coroutines.*
import uk.gov.dwp.dataworks.logging.DataworksLogger
import java.time.Duration
import kotlin.system.measureTimeMillis


fun shovelAsync(consumer: KafkaConsumer<ByteArray, ByteArray>,
                metadataClient: MetadataStoreClient,
                awsS3Service: AwsS3Service,
                pollTimeout: Duration) =
    GlobalScope.async {
        val logger = DataworksLogger.getLogger("shovelAsync")
        val parser = MessageParser()
        val validator = Validator()
        val converter = Converter()
        val listProcessor = ListProcessor(validator, converter)
        var batchCount = 0
        while (isActive) {
            try {
                consumer.subscribe(Config.Kafka.topicRegex)
                val records = consumer.poll(pollTimeout)
                if (records.count() > 0) {
                    HbaseClient.connect().use { hbase ->
                        val timeTaken = measureTimeMillis {
                            listProcessor.processRecords(hbase, consumer, metadataClient, awsS3Service, parser, records)
                        }
                        logger.info("Processed batch", "time_taken" to "$timeTaken", "size" to "${records.count()}")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error reading from kafka", e, "error" to (e.message ?: ""))
                cancel(CancellationException("Error reading from kafka ${e.message}", e))
            }
        }
    }


fun batchCountIsMultipleOfReportFrequency(batchCount: Int): Boolean = (batchCount % Config.Shovel.reportFrequency) == 0

