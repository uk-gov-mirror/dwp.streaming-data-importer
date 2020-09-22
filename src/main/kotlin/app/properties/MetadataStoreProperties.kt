package app.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="metadatastore")
data class MetadataStoreProperties(val endpoint: String = "",
                                   val port: String = "",
                                   val databaseName: String = "metadatastore",
                                   val user: String  = "",
                                   val passwordSecretName: String = "metadata-store-reconciler",
                                   val caCertPath: String = "./AmazonRootCA1.pem")
