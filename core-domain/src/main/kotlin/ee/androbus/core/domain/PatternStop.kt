package ee.androbus.core.domain

data class PatternStop(
    val sequence: Int,
    val stopPointId: StopPointId,
) {
    init {
        require(sequence > 0) { "PatternStop sequence must be positive." }
    }
}
