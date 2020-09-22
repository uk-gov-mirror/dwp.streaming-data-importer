class HbaseWriteException(message: String) : Exception(message)
class InvalidMessageException(message: String, cause: Throwable) : Exception(message, cause)
