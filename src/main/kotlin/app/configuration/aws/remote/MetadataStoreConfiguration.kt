package app.configuration.aws.remote

import app.properties.MetadataStoreProperties
import app.services.SecretService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

@Configuration
@Profile("!LOCALSTACK")
class MetadataStoreConfiguration(private val properties: MetadataStoreProperties,
                                 private val secretService: SecretService) {

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
                put("ssl_ca_path", it.caCertPath)
                put("ssl_ca", File(it.caCertPath).readText(Charsets.UTF_8))
                put("ssl_verify_cert", true)
                put("password", secretService.secret(it.passwordSecretName))
            }
        }
    }
}
