package style

import kotlinx.serialization.Serializable

@Serializable
data class Font(val name: String = "default", val format: String = "otf", val path: String = "data/fonts") {
    companion object {
        val DEFAULT = Font()
    }
}
