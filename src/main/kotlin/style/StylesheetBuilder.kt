package style

import ui.UIHost

class StylesheetBuilder {
    val stack: StyleStack get() = UIHost.current.style

    operator fun String.invoke(vararg modifiers: StyleModifier, block: Style.() -> Unit = {}) {
        for (m in modifiers) {
            stack.add("$this${m.suffix}", block)
        }
    }

    operator fun String.invoke(block: Style.() -> Unit = {}) {
        stack.add(this, block)
    }

    operator fun List<String>.invoke(block: Style.() -> Unit = {}) {
        for (sel in this) {
            stack.add(sel, block)
        }
    }

    val hover = StyleModifier.HOVER
    val focus = StyleModifier.FOCUS
}

fun stylesheet(block: StylesheetBuilder.() -> Unit): Stylesheet {
    val sb = StylesheetBuilder()
    sb.block()
    return sb.stack.toStylesheet()
}

fun style(block: Style.() -> Unit): Style {
    return Style().apply { block() }
}