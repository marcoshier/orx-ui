package widgets

import ui.UIElement


/** Radio */

class RadioBuilder(val wbuilder: WidgetBuilder) {
    val elements = mutableListOf<UIElement>()

    fun add(el: UIElement) {
        elements.add(el)
        wbuilder.add(el)
    }

    fun emit() = toRadio(elements)
}

fun WidgetBuilder.asRadio(build: RadioBuilder.() -> Unit = {}): List<UIElement> {
    val builder = RadioBuilder(this)
    builder.build()
    return builder.emit()
}

fun asRadio(vararg elements: UIElement): List<UIElement> {
    val els = elements.toList()
    return toRadio(els)
}

fun List<UIElement>.asRadio(): List<UIElement> = toRadio(this)

private fun toRadio(elements: List<UIElement>): List<UIElement> {
    elements.forEach { element ->
        element.clicked.listen {
            elements.forEach { it.isSelected = (it === element) }
        }
    }

    if (elements.none { it.isSelected }) {
        elements.firstOrNull()?.isSelected = true
    }
    return elements
}


/** Checkbox */

class CheckboxBuilder(val wbuilder: WidgetBuilder) {
    val elements = mutableListOf<UIElement>()

    fun add(el: UIElement) {
        el.clicked.listen { el.isSelected = !el.isSelected }
        elements.add(el)
        wbuilder.add(el)
    }
}

fun WidgetBuilder.asCheckbox(build: CheckboxBuilder.() -> Unit = {}): List<UIElement> {
    val builder = CheckboxBuilder(this)
    builder.build()
    return builder.elements
}

fun asCheckbox(vararg elements: UIElement): List<UIElement> {
    val els = elements.toList()
    return toCheckbox(els)
}

fun List<UIElement>.asCheckbox() = toCheckbox(this)

private fun toCheckbox(elements: List<UIElement>): List<UIElement> {
    elements.forEach { element ->
        element.clicked.listen { element.isSelected = !element.isSelected }
    }
    return elements
}
