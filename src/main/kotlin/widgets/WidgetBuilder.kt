package widgets

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement

enum class Direction {
    DOWN,
    RIGHT,
    UP,
    LEFT
}

class WidgetBuilder(
    val label: String,
    val bounds: Rectangle,
    val zIndex: Int,
    val parent: Widget? = null
) {
    var widget = WidgetImpl(label, bounds, zIndex, parent)

    var itemX = 0.0
        private set

    var itemY = 0.0
        private set

    var itemWidth = widget.style.minElementWidth
        private set

    var itemHeight = widget.style.minElementHeight
        private set

    fun step(direction: Direction) {
        val elementBounds = widget.elements.map { it.bounds }
        val widgetBounds = widget.widgets.map { it.bounds }

        val occupiedBounds = elementBounds + widgetBounds
        val empty = occupiedBounds.isEmpty()

        when (direction) {
            Direction.DOWN -> {
                itemX = bounds.x + paddingX

                itemY =
                    (occupiedBounds.maxOfOrNull { it.y + it.height } ?: bounds.y) +
                            if (empty) paddingY else gutterY

                itemWidth = bounds.width - 2 * paddingX
                itemHeight = minElementHeight
            }

            Direction.UP -> {
                itemX = bounds.x + paddingX

                val topEdge =
                    occupiedBounds.minOfOrNull { it.y }
                        ?: (bounds.y + bounds.height)

                itemWidth = bounds.width - 2 * paddingX
                itemHeight = minElementHeight

                itemY =
                    topEdge -
                            (if (empty) paddingY else gutterY) -
                            itemHeight
            }

            Direction.RIGHT -> {
                itemY = bounds.y + paddingY

                itemX =
                    (occupiedBounds.maxOfOrNull { it.x + it.width } ?: bounds.x) +
                            if (empty) paddingX else gutterX

                itemWidth = minElementWidth
                itemHeight = bounds.height - 2 * paddingY
            }

            Direction.LEFT -> {
                itemY = bounds.y + paddingY

                val leftEdge =
                    occupiedBounds.minOfOrNull { it.x }
                        ?: (bounds.x + bounds.width)

                itemWidth = minElementWidth
                itemHeight = bounds.height - 2 * paddingY

                itemX =
                    leftEdge -
                            (if (empty) paddingX else gutterX) -
                            itemWidth
            }
        }
    }

    fun <T> append(direction: Direction, block: WidgetBuilder.() -> T): T {
        step(direction)
        return block()
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

    var gutterX = 0.0
    var gutterY = 0.0
    var paddingX by widget.style::paddingX
    var paddingY by widget.style::paddingY
    var padding: Vector2
        get() = Vector2(paddingX, paddingY)
        set(value) {
            paddingX = value.x
            paddingY = value.y
        }
    var background by widget.style::background
    var stroke by widget.style::stroke
    var minElementWidth by widget.style::minElementWidth
    var minElementHeight by widget.style::minElementHeight
    var maxElementWidth by widget.style::maxElementWidth
    var maxElementHeight by widget.style::maxElementHeight

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

fun WidgetBuilder.widget(
    bounds: Rectangle,
    label: String = "",
    zIndex: Int = 0,
    build: WidgetBuilder.() -> Unit = {},
): WidgetImpl {
    val childBuilder = WidgetBuilder(label, bounds, zIndex, this.widget)
    childBuilder.gutterX = gutterX
    childBuilder.gutterY = gutterY
    childBuilder.build()
    val child = childBuilder.widget
    this.widget.widgets.add(child)
    return child
}
