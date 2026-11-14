package style

enum class StyleModifier(val suffix: String) {
    HOVER(":hover"),
    ACTIVE(":active"),
    FOCUS(":focus"),
    SELECTED(":selected")
}

fun selectorOf(base: String, modifiers: Collection<StyleModifier>) =
    base + modifiers.distinct().sortedBy { it.ordinal }.joinToString("") { it.suffix }
