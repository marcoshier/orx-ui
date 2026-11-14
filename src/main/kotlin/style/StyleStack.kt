package style

enum class StyleLayer { DEFAULTS, USER }

class StyleStack {

    private class Rule(
        val selector: String,
        val layer: StyleLayer,
        val order: Int,
        val block: Style.() -> Unit,
    ) {
        val base = selector.substringBefore(':')

        val required: Set<StyleModifier> = selector.split(':').drop(1)
            .mapNotNull { name -> StyleModifier.entries.firstOrNull { it.suffix == ":$name" } }
            .toSet()

        val rank = when {
            base.startsWith("#") -> 2
            base == "*" -> 0
            else -> 1
        }
    }

    private val rules = mutableListOf<Rule>()

    fun add(selector: String, block: Style.() -> Unit, layer: StyleLayer = StyleLayer.USER) {
        rules += Rule(selector, layer, rules.size, block)
    }

    fun add(sheet: Stylesheet, layer: StyleLayer = StyleLayer.USER) =
        sheet.rules.forEach { (selector, block) -> add(selector, block, layer) }

    fun toStylesheet(): Stylesheet =
        Stylesheet(rules.filter { it.layer == StyleLayer.USER }.map { it.selector to it.block })

    fun resolve(bases: Set<String>, states: Set<StyleModifier>, inline: Style? = null): Style {
        val result = Style()
        rules.filter { it.base in bases && states.containsAll(it.required) }
            .sortedWith(compareBy({ it.layer }, { it.rank }, { it.required.size }, { it.order }))
            .forEach { it.block(result) }
        inline?.let { result.apply(it) }
        return result
    }
}