package style

import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

class StyleStack {
    private val rules = mutableListOf<Pair<String, Style.() -> Unit>>()

    var default = Style()

    @OptIn(ExperimentalSerializationApi::class)
    fun toStylesheet(): Stylesheet {
        val map = LinkedHashMap<String, Style>()
        for ((name, block) in rules) {
            map[name] = Style().apply(block)
        }
        return Stylesheet(map)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun fromStylesheet(stylesheet: Stylesheet) {
        for ((name, style) in stylesheet) {
            rules.add(name to { apply(style) })
        }
    }

    fun add(selector: String, block: Style.() -> Unit) {
        rules += selector to block
    }

    fun resolve(selectors: Set<String>, current: Style? = null): Style {
        val result = default.copy()

        rules.filter { (sel, _) -> sel == "*" || sel in selectors }
            .sortedBy { (if (it.first.startsWith("#")) 2 else 0) + (if (':' in it.first) 1 else 0) }
            .forEach { (_, block) -> result.block() }

        current?.let { result.apply(it) }

        return result
    }
}
