package style

import kotlinx.serialization.Serializable

@Serializable
data class Font(val name: String = "IBMPlexSans-Regular", val format: String = "ttf", val path: String = "data/fonts") {
    companion object {
        val DEFAULT = Font()
    }
}
