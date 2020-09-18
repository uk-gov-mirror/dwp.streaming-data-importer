import Config.Kafka.dlqTopic
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

open class RecordProcessor(private val validator: Validator, private val converter: Converter) {

    private val textUtils = TextUtils()

    open fun processRecord(record: ConsumerRecord<ByteArray, ByteArray>, hbase: HbaseClient, parser: MessageParser) {

        convertAndValidateJsonRecord(record)?.let { json ->
            val formattedKey = parser.generateKeyFromRecordBody(json)
            if (formattedKey.isEmpty()) {
                logger.warn("Empty key for record", "record", getDataStringForRecord(record))
                return
            }
            writeRecordToHbase(json, record, hbase, formattedKey)
        }
    }

    fun convertAndValidateJsonRecord(record: ConsumerRecord<ByteArray, ByteArray>): JsonObject? {
        try {
            converter.convertToJson(record.value()).let { json ->
                validator.validate(json.toJsonString())
                return json
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Could not parse message body", "record", getDataStringForRecord(record))
            sendMessageToDlq(record, "Invalid json")
            return null
        } catch (e: InvalidMessageException) {
            logger.warn("Schema validation error", "record", getDataStringForRecord(record), "message", "${e.message}")
            sendMessageToDlq(record, "Invalid schema for ${getDataStringForRecord(record)}: ${e.message}")
            return null
        }
    }

    private fun writeRecordToHbase(json: JsonObject, record: ConsumerRecord<ByteArray, ByteArray>, hbase: HbaseClient, formattedKey: ByteArray) {
        try {
            val (lastModifiedTimestampStr, fieldTimestampCreatedFrom) = converter.getLastModifiedTimestamp(json)
            val message = json["message"] as JsonObject
            message["timestamp_created_from"] = fieldTimestampCreatedFrom

            val lastModifiedTimestampLong = converter.getTimestampAsLong(lastModifiedTimestampStr)
            val matcher = textUtils.topicNameTableMatcher(record.topic())
            if (matcher != null) {
                val namespace = matcher.groupValues[1]
                val tableName = matcher.groupValues[2]
                val qualifiedTableName = targetTable(namespace, tableName)
                logger.debug(
                    "Written record to hbase", "record", getDataStringForRecord(record),
                    "formattedKey", String(formattedKey)
                )
                val recordBodyJson = json.toJsonString()
                hbase.put(qualifiedTableName!!, formattedKey, recordBodyJson.toByteArray(), lastModifiedTimestampLong)
            } else {
                logger.error("Could not derive table name from topic", "topic", record.topic())
            }
        } catch (e: Exception) {
            logger.error("Error writing record to HBase", e, "record", getDataStringForRecord(record))
            throw HbaseWriteException("Error writing record to HBase: $e")
        }
    }

    private fun targetTable(namespace: String, tableName: String) =
        textUtils.coalescedName("$namespace:$tableName")?.replace("-", "_")

    open fun sendMessageToDlq(record: ConsumerRecord<ByteArray, ByteArray>, reason: String) {
        val body = record.value()
        logger.warn(
            "Error processing record, sending to dlq",
            "reason", reason, "key", String(record.key())
        )
        try {
            val malformedRecord = MalformedRecord(String(record.key()), String(body), reason)
            val jsonString = Klaxon().toJsonString(malformedRecord)
            val producerRecord = ProducerRecord(
                dlqTopic,
                null,
                null,
                record.key(),
                jsonString.toByteArray(),
                null
            )
            val metadata = DlqProducer.getInstance()?.send(producerRecord)?.get()
            logger.info(
                "Sending message to dlq",
                "key",
                String(record.key()),
                "topic",
                metadata?.topic().toString(),
                "offset",
                "${metadata?.offset()}"
            )
        } catch (e: Exception) {
            logger.error(
                "Error sending message to dlq",
                "key", String(record.key()), "topic", record.topic(), "offset", "${record.offset()}"
            )
            throw DlqException("Exception while sending message to DLQ : $e")
        }
    }

    fun getObjectAsByteArray(obj: MalformedRecord): ByteArray? {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)
        oos.writeObject(obj)
        oos.flush()
        return bos.toByteArray()
    }

    companion object {
        val logger: JsonLoggerWrapper = JsonLoggerWrapper.getLogger(RecordProcessor::class.toString())
    }
}

fun getDataStringForRecord(record: ConsumerRecord<ByteArray, ByteArray>): String {
    return "${String(record.key() ?: ByteArray(0))}:${record.topic()}:${record.partition()}:${record.offset()}"
}
