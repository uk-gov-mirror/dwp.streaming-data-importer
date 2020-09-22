package app.domain

data class HBasePayload(val table: String, val body: String, val version: Long)
data class StreamedBatch(val payloads: List<HBasePayload>)
