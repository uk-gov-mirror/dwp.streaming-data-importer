package app.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@ConfigurationProperties(prefix="metadatastore")
@Profile("!LOCALSTACK")
data class AwsMetadataStoreProperties(val endpoint: String = "",
                                      val port: String = "",
                                      val databaseName: String = "metadatastore",
                                      val user: String  = "",
                                      val passwordSecretName: String = "metadata-store-reconciler",
                                      val caCertPath: String = "./AmazonRootCA1.pem")
