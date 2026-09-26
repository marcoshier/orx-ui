package widgets

import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement

class WidgetBuilder(
    val label: String,
    val bounds: Rectangle,
    val zIndex: Int
) {
    val widget = WidgetImpl(label, bounds, zIndex)

    var marginX = 0.0
    var marginY = 0.0
    var gutterX = 0.0
    var gutterY = 0.0

    var currentX = 0.0
        private set

    var currentY = 0.0
        private set

    var minElementWidth = 5.0
    var maxElementWidth = 10.0

    var minElementHeight = 5.0
    var maxElementHeight = 10.0

    var currentElementWidth = minElementWidth
        private set

    var currentElementHeight = minElementHeight
        private set

    fun append(direction: Direction, block: WidgetBuilder.() -> UIElement) {
        val empty = widget.elements.isEmpty()

        when (direction) {
            Direction.DOWN -> {
                currentX = bounds.x + marginX
                currentY = (widget.elements.maxOfOrNull { it.bounds.y + it.bounds.height } ?: bounds.y) +
                        (if (empty) marginY else gutterY)
                currentElementWidth = bounds.width - 2 * marginX
                currentElementHeight = minElementHeight
            }

            Direction.UP -> {
                currentX = bounds.x + marginX
                val topEdge = widget.elements.minOfOrNull { it.bounds.y } ?: (bounds.y + bounds.height)
                currentElementWidth = bounds.width - 2 * marginX
                currentElementHeight = minElementHeight
                currentY = topEdge - (if (empty) marginY else gutterY) - currentElementHeight
            }

            Direction.RIGHT -> {
                currentY = bounds.y + marginY
                currentX = (widget.elements.maxOfOrNull { it.bounds.x + it.bounds.width } ?: bounds.x) +
                        (if (empty) marginX else gutterX)
                currentElementWidth = minElementWidth
                currentElementHeight = bounds.height - 2 * marginY
            }

            Direction.LEFT -> {
                currentY = bounds.y + marginY
                val leftEdge = widget.elements.minOfOrNull { it.bounds.x } ?: (bounds.x + bounds.width)
                currentElementWidth = minElementWidth
                currentElementHeight = bounds.height - 2 * marginY
                currentX = leftEdge - (if (empty) marginX else gutterX) - currentElementWidth
            }
        }

        block()
    }

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
