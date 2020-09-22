package app.configuration.aws.local

import app.properties.MetadataStoreProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

@Configuration
@Profile("LOCALSTACK")
class MetadataStoreConfiguration(private val properties: MetadataStoreProperties) {

    @Bean
    fun metadataStoreConnection(): Connection = DriverManager.getConnection(databaseUrl, databaseProperties)

    private val databaseUrl by lazy {
        with (properties) {
            "jdbc:mysql://$endpoint:$port/$databaseName"
        }
    }

    private val databaseProperties by lazy {
        Properties().apply {
            properties.let {
                put("user", it.user)
                put("password", "password")
            }
        }
    }
}
