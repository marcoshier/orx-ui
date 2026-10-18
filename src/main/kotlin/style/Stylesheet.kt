package style

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import lib.hoconConfigOptions
import java.io.File

private val logger = KotlinLogging.logger {  }

@Serializable
data class Stylesheet(
    val rules: LinkedHashMap<String, Style> = LinkedHashMap()
) : Map<String, Style> by rules {

    // fromCss, toCss

    @OptIn(ExperimentalSerializationApi::class)
    fun toFile(file: File) {
        val config = Hocon.encodeToConfig(serializer(), this)
        val hocon = config.root().render(hoconConfigOptions)
        file.writeText(hocon)
        logger.info { "Stylesheet saved in $file" }
    }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromFile(file: File): Stylesheet {
            val config = ConfigFactory.parseFile(file)
            val stylesheet = Hocon.decodeFromConfig(serializer(), config)

            if (config.hasPathOrNull("rules")) {
                val rulesConfig = config.getConfig("rules")
                for (name in rulesConfig.root().keys) {
                    val ruleVal = rulesConfig.root()[name]
                    if (ruleVal is ConfigObject) {
                        val ruleConfig = ruleVal.toConfig()
                        val style = stylesheet.rules[name] ?: continue
                        style.setProperties = ruleConfig.root().keys

                        for (prop in styleColorProperties) {
                            if (ruleConfig.hasPathOrNull(prop.name) && ruleConfig.getIsNull(prop.name)) {
                                prop.set(style, null)
                            }
                        }
                    }
                }
            }

            return stylesheet
        }
    }
}
