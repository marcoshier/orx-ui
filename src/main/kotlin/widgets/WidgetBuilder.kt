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


    fun add(element: UIElement): UIElement {
        element.parent = widget
        widget.elements.add(element)
        return element
    }

    fun add(vararg element: UIElement): List<UIElement> {
        return element.toList().map {
            it.parent = widget
            widget.elements.add(it)
            it
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
