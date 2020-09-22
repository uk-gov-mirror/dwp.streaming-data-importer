package app.configuration

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@ConfigurationProperties(prefix = "aws")
@Profile("!LOCALSTACK")
class AwsConfiguration(private val region: String = "eu-west-2",
                       private val maximumConnections: Int = 1000,
                       private val timeout: Int = 1800000) {

    @Bean
    fun amazonS3(): AmazonS3 =
            AmazonS3ClientBuilder.standard().run {
                withCredentials(DefaultAWSCredentialsProviderChain())
                withRegion(regions())
                withClientConfiguration(clientConfiguration())
                build()
            }

    @Bean
    fun awsSecretsManager(): AWSSecretsManager = AWSSecretsManagerClientBuilder.standard().withRegion(region).build()

    private fun regions() =
            region.toUpperCase().replace("-", "_").run {
                Regions.valueOf(this)
            }


    private fun clientConfiguration() =
            ClientConfiguration().apply {
                maxConnections = maximumConnections
                socketTimeout = timeout
            }

}
