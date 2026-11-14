package style

import com.typesafe.config.ConfigFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import lib.hoconConfigOptions
import java.io.File

private val logger = KotlinLogging.logger {  }

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Stylesheet(
    internal val rules: List<Pair<String, Style.() -> Unit>>
) {

    // fromCss, toCss

    operator fun plus(other: Stylesheet) = Stylesheet(rules + other.rules)

    fun toFile(file: File) {
        val map = LinkedHashMap<String, Style>()
        for ((sel, block) in rules) map.getOrPut(sel) { Style() }.apply(block)
        val config = Hocon.encodeToConfig(StylesheetFile.serializer(), StylesheetFile(map))
        file.writeText(config.root().render(hoconConfigOptions))
    }

    companion object {
        fun fromFile(file: File): Stylesheet {
            val data = Hocon.decodeFromConfig(StylesheetFile.serializer(), ConfigFactory.parseFile(file))
            return Stylesheet(data.rules.map { (sel, style) -> sel to { apply(style) } })
        }
    }
}

@Serializable
internal class StylesheetFile(val rules: LinkedHashMap<String, Style>)
