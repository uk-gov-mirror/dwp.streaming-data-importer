package app.services

interface SecretService {
    fun secret(name: String): String
}
