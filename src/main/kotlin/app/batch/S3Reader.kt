package app.batch

import com.amazonaws.services.s3.AmazonS3
import org.springframework.batch.item.ItemReader
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class S3Reader(private val amazonS3: AmazonS3): ItemReader<ByteArray> {

    override fun read(): ByteArray? {
        TODO("Not yet implemented $amazonS3")
    }

}
