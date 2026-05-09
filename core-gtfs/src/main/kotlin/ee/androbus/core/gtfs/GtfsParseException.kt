package ee.androbus.core.gtfs

class GtfsParseException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
