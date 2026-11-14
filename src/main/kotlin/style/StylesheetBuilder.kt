package style

import ui.UIHost

class StylesheetBuilder {
    internal val entries = mutableListOf<Pair<String, Style.() -> Unit>>()

    operator fun String.invoke(vararg modifiers: StyleModifier, block: Style.() -> Unit) {
        entries += selectorOf(this, modifiers.toList()) to block
    }

    operator fun List<String>.invoke(block: Style.() -> Unit) {
        forEach { entries += it to block }
    }

    val hover = StyleModifier.HOVER
    val active = StyleModifier.ACTIVE
    val focus = StyleModifier.FOCUS
    val selected = StyleModifier.SELECTED
}
fun buildStylesheet(block: StylesheetBuilder.() -> Unit): Stylesheet =
    Stylesheet(StylesheetBuilder().apply(block).entries.toList())

fun stylesheet(layer: StyleLayer = StyleLayer.USER, block: StylesheetBuilder.() -> Unit): Stylesheet =
    buildStylesheet(block).also { UIHost.current.styleStack.add(it, layer) }

fun defaultStylesheet(block: StylesheetBuilder.() -> Unit) = stylesheet(StyleLayer.DEFAULTS, block)

fun style(block: Style.() -> Unit): Style = Style().apply(block)

fun emptyStylesheet() = buildStylesheet {  }