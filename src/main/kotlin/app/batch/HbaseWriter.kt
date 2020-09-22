package app.batch

import app.domain.StreamedBatch
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class HbaseWriter: ItemWriter<StreamedBatch> {
    override fun write(items: MutableList<out StreamedBatch>) {
        TODO("Not yet implemented")
    }
}
