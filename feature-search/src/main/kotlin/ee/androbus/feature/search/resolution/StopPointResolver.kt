package ee.androbus.feature.search.resolution

interface StopPointResolver {
    fun resolve(input: StopPointResolutionInput): StopPointResolutionResult
}

