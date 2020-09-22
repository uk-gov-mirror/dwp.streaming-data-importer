import com.amazonaws.services.secretsmanager.*
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.fasterxml.jackson.databind.ObjectMapper
import uk.gov.dwp.dataworks.logging.DataworksLogger

class AWSSecretHelper: SecretHelperInterface {

    override fun getSecret(secretName: String): String? {
        logger.info("Getting value from aws secret manager", "secret_name" to secretName)
        val region = Config.SecretManager.properties["region"].toString()
        val client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build()
        val getSecretValueRequest = GetSecretValueRequest().withSecretId(secretName)
        val getSecretValueResult = client.getSecretValue(getSecretValueRequest)
        logger.debug("Successfully got value from aws secret manager", "secret_name" to secretName)
        val secretString = getSecretValueResult.secretString
        @Suppress("UNCHECKED_CAST")
        val map = ObjectMapper().readValue(secretString, Map::class.java) as Map<String, String>
        return map["password"]
    }

    companion object {
        val logger = DataworksLogger.getLogger(AWSSecretHelper::class.java.toString())
    }
}
