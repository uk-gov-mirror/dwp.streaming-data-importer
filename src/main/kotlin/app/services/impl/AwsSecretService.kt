package app.services.impl

import app.services.SecretService
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!LOCALSTACK")
class AwsSecretService(private val awsSecretsManager: AWSSecretsManager): SecretService {

    override fun secret(name: String): String =
        awsSecretsManager.getSecretValue(secretValueRequest(name)).run {
            secretString.run {
                secretMap()["password"] ?: error("No password in secret request result")
            }
        }

    private fun String?.secretMap() =
            ObjectMapper().readValue(this, Map::class.java) as Map<String, String>

    private fun secretValueRequest(name: String): GetSecretValueRequest = GetSecretValueRequest().apply { withSecretId(name) }
}
