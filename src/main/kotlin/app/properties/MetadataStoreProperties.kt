package app.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="metadatastore")
data class MetadataStoreProperties(var endpoint: String = "metadatastore",
                                   val port: Int = 3306,
                                   val databaseName: String = "metadatastore",
                                   var user: String?  = "",
                                   val passwordSecretName: String = "metadata-store-reconciler",
                                   val caCertPath: String = "./AmazonRootCA1.pem")
