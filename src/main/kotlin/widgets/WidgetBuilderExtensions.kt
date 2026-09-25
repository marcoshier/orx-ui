package widgets

import ui.UIElement


/** Radio */

fun WidgetBuilder.asRadio(vararg elements: UIElement) {
    elements.forEach { element ->
        element.clicked.listen {
            elements.forEach { it.isSelected = (it === element) }
        }
    }
    if (elements.none { it.isSelected }) {
        elements.firstOrNull()?.isSelected = true
    }

    addAll(elements.toList())
}

context(builder: WidgetBuilder)
fun List<UIElement>.asRadio() = builder.asRadio(*this.toTypedArray())


/** Checkbox */

fun WidgetBuilder.asCheckbox(vararg elements: UIElement) {
    elements.forEach { element ->
        element.clicked.listen { element.isSelected = !element.isSelected }
    }

    addAll(elements.toList())
}

context(builder: WidgetBuilder)
fun List<UIElement>.asCheckbox() = builder.asCheckbox(*this.toTypedArray())
