package style

class StyleBuilder {
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

fun style(block: StyleBuilder.() -> Unit): Stylesheet {
    val sb = StyleBuilder()
    sb.block()
    return sb.stack.toStylesheet()
}
