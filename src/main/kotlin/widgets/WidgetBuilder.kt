package widgets

import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement

class WidgetBuilder(
    label: String,
    bounds: Rectangle,
    zIndex: Int
) {
    val widget = WidgetImpl(label, bounds, zIndex)

    fun add(element: UIElement) {
        element.parent = widget
        widget.elements.add(element)
    }

    fun addAll(elements: List<UIElement>) {
        for (element in elements) {
            element.parent = widget
            widget.elements.add(element)
        }
    }

    fun addAll(vararg elements: UIElement) {
        for (element in elements) {
            element.parent = widget
            widget.elements.add(element)
        }
    }
}

fun widget(
    bounds: Rectangle,
    label: String = "",
    zIndex: Int = 0,
    build: WidgetBuilder.() -> Unit = {},
): WidgetImpl {
    val builder = WidgetBuilder(label, bounds, zIndex)
    builder.build()
    val widget = builder.widget
    registerWidget(widget)
    return widget
}
