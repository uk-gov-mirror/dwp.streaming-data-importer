package app.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "aws")
data class AwsProperties(val region: String = "eu-west-2",
                         val maximumConnections: Int = 1000,
                         val timeout: Int = 1800000)
