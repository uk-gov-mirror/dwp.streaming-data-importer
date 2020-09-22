package app.configuration

import app.domain.StreamedBatch
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor

@Configuration
class JobConfiguration {

    @Bean
    fun importJob(step: Step): Job =
        jobBuilderFactory.get("streamDataImportJob")
            .incrementer(RunIdIncrementer())
            .flow(step)
            .end()
            .build()

    @Bean
    fun step(): TaskletStep =
        stepBuilderFactory.get("step")
            .chunk<ByteArray, StreamedBatch>(chunkSize.toInt())
            .reader(itemReader)
            .processor(processor())
            .writer(itemWriter)
            .taskExecutor(taskExecutor())
            .throttleLimit(throttleLimit.toInt())
            .build()

    @Bean
    fun taskExecutor() = SimpleAsyncTaskExecutor("uc-historic-data-importer").apply {
        concurrencyLimit = Integer.parseInt(threadCount)
    }

    fun processor(): ItemProcessor<ByteArray, StreamedBatch> =
            CompositeItemProcessor<ByteArray, StreamedBatch>().apply {
                setDelegates(listOf(itemProcessor))
            }


    @Autowired
    private lateinit var itemReader: ItemReader<ByteArray>


    @Autowired
    private lateinit var itemProcessor: ItemProcessor<ByteArray, StreamedBatch>

    @Autowired
    private lateinit var itemWriter: ItemWriter<StreamedBatch>

    @Autowired
    lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    lateinit var stepBuilderFactory: StepBuilderFactory

    @Value("\${thread.count:10}")
    lateinit var threadCount: String

    @Value("\${throttle.limit:50}")
    lateinit var throttleLimit: String

    @Value("\${chunk.size:1}")
    lateinit var chunkSize: String

}
