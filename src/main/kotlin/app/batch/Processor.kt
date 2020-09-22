package app.batch

import app.domain.StreamedBatch
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class Processor: ItemProcessor<ByteArray, StreamedBatch> {
    override fun process(item: ByteArray): StreamedBatch? {
        TODO("Not yet implemented")
    }
}
