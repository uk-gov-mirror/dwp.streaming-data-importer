package app.configuration.aws.remote

import app.properties.AwsProperties
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!LOCALSTACK")
class ClientConfiguration(private val properties: AwsProperties) {

    @Bean
    fun amazonS3(): AmazonS3 =
            AmazonS3ClientBuilder.standard().run {
                withCredentials(DefaultAWSCredentialsProviderChain())
                withRegion(regions())
                withClientConfiguration(clientConfiguration())
                build()
            }

    @Bean
    fun awsSecretsManager(): AWSSecretsManager =
            AWSSecretsManagerClientBuilder.standard().withRegion(regions()).build()

    private fun regions() =
            properties.region.toUpperCase().replace("-", "_").run {
                Regions.valueOf(this)
            }


    private fun clientConfiguration() =
            ClientConfiguration().apply {
                maxConnections = properties.maximumConnections
                socketTimeout = properties.timeout
            }

}
