package widget

import ui.UIElement

/**
 * Radio: exactly one element of the group is selected at a time.
 *
 * Usage inside a widget:
 *   asRadio {
 *       button(r0) { label = "A" }
 *       button(r1) { label = "B" }
 *   }
 *
 * Use plain elements; state-bound ones (e.g. button(state::on, …)) set
 * isSelected themselves every frame and would override the group.
 */
fun WidgetBuilder.asRadio(build: WidgetBuilder.() -> Unit): List<UIElement> =
    toRadio(collectAdded(build))

fun asRadio(vararg elements: UIElement): List<UIElement> =
    toRadio(elements.toList())

fun List<UIElement>.asRadio(): List<UIElement> =
    toRadio(this)

private fun toRadio(elements: List<UIElement>): List<UIElement> {
    elements.forEach { element ->
        element.buttonDown.listen {
            elements.forEach { it.isSelected = (it === element) }
        }
    }

    if (elements.none { it.isSelected }) {
        elements.firstOrNull()?.isSelected = true
    }
    return elements
}

/**
 * Checkbox: every element of the group toggles independently.
 *
 * Usage inside a widget:
 *   asCheckbox {
 *       button(r0) { label = "A" }
 *       button(r1) { label = "B" }
 *   }
 */
fun WidgetBuilder.asCheckbox(build: WidgetBuilder.() -> Unit): List<UIElement> =
    toCheckbox(collectAdded(build))

fun asCheckbox(vararg elements: UIElement): List<UIElement> =
    toCheckbox(elements.toList())

fun List<UIElement>.asCheckbox(): List<UIElement> =
    toCheckbox(this)

private fun toCheckbox(elements: List<UIElement>): List<UIElement> {
    elements.forEach { element ->
        element.buttonDown.listen { element.isSelected = !element.isSelected }
    }
    return elements
}

/** Runs [build] as a normal widget builder and returns the elements it added. */
private fun WidgetBuilder.collectAdded(build: WidgetBuilder.() -> Unit): List<UIElement> {
    val before = widget.elements.size
    build()
    return widget.elements.drop(before)
}