package ee.androbus.core.domain

@JvmInline
value class StopGroupId(val value: String) {
    init {
        require(value.isNotBlank()) { "StopGroupId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class StopPointId(val value: String) {
    init {
        require(value.isNotBlank()) { "StopPointId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class RouteLineId(val value: String) {
    init {
        require(value.isNotBlank()) { "RouteLineId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class RoutePatternId(val value: String) {
    init {
        require(value.isNotBlank()) { "RoutePatternId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class TripId(val value: String) {
    init {
        require(value.isNotBlank()) { "TripId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class ServiceId(val value: String) {
    init {
        require(value.isNotBlank()) { "ServiceId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class AgencyId(val value: String) {
    init {
        require(value.isNotBlank()) { "AgencyId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class FeedId(val value: String) {
    init {
        require(value.isNotBlank()) { "FeedId cannot be blank." }
    }

    override fun toString(): String = value
}

@JvmInline
value class CityId(val value: String) {
    init {
        require(value.isNotBlank()) { "CityId cannot be blank." }
    }

    override fun toString(): String = value
}
