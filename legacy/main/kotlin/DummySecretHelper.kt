class DummySecretHelper: SecretHelperInterface {
    override fun getSecret(secretName: String) =
            if (secretName == "password") "password" else getEnv("DUMMY_SECRET_${secretName.toUpperCase()}") ?: "NOT_SET"
}
