package app.configuration.localstack

import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("LOCALSTACK")
class ClientConfiguration {

    @Bean
    fun amazonS3(): AmazonS3 =
            AmazonS3ClientBuilder.standard().run {
                withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("http://localhost:4566", "eu-west-2"))
                withClientConfiguration(ClientConfiguration().withProtocol(Protocol.HTTP))
                withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("accessKey", "secretKey")))
                withPathStyleAccessEnabled(true)
                disableChunkedEncoding()
                build()
            }
}
