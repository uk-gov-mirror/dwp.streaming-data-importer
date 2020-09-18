import com.amazonaws.services.secretsmanager.*
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.fasterxml.jackson.databind.ObjectMapper

class AWSSecretHelper: SecretHelperInterface {

    companion object {
        val logger: JsonLoggerWrapper = JsonLoggerWrapper.getLogger(AWSSecretHelper::class.toString())
    }

    override fun getSecret(secretName: String): String? {

        logger.info("Getting value from aws secret manager", "secret_name", secretName)

        try {
            val region = Config.SecretManager.properties["region"].toString()
            val client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build()
            val getSecretValueRequest = GetSecretValueRequest().withSecretId(secretName)

            val getSecretValueResult = client.getSecretValue(getSecretValueRequest)

            logger.debug("Successfully got value from aws secret manager", "secret_name", secretName)

            val secretString = getSecretValueResult.secretString

            @Suppress("UNCHECKED_CAST")
            val map = ObjectMapper().readValue(secretString, Map::class.java) as Map<String, String>

            return map["password"]
        } catch (e: Exception) {
            logger.error("Failed to get aws secret manager result", e, "secret_name", secretName)
            throw e
        }
    }
}
