package widget

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement
import walk

enum class Direction {
    DOWN,
    RIGHT,
    UP,
    LEFT
}

class WidgetBuilder(
    val bounds: Rectangle,
    val zIndex: Int,
    val parent: Widget? = null
) {
    var widget = Widget(bounds, zIndex, parent)

    var currentX = initialX()
        private set
    var currentY = initialY()
        private set
    var elementWidth = initialWidth()
    var elementHeight = initialHeight()

    val currentElementBounds: Rectangle
        get() = Rectangle(currentX, currentY, elementWidth, elementHeight)

    private fun initialX(): Double {
        return widget.bounds.x + widget.style.paddingX + widget.style.marginX
    }

    private fun initialY(): Double {
        return widget.bounds.y + widget.style.paddingY + widget.style.marginY
    }

    private fun initialWidth(): Double {
        val wpx = widget.style.paddingX * 2
        val wmx = widget.style.marginX * 2
        val minw = widget.style.minWidth
        val maxw = widget.style.maxWidth
        return (widget.bounds.width - wpx - wmx).coerceIn(minw, maxw)
    }

    private fun initialHeight(): Double {
        val wpy = widget.style.paddingY * 2
        val wmy = widget.style.marginY * 2
        val minh = widget.style.minHeight
        val maxh = widget.style.maxHeight
        return (widget.bounds.height - wpy - wmy).coerceIn(minh, maxh)
    }

    fun step(direction: Direction, spacing: Double = 0.0) {
        val elementBounds = widget.walk().map { it.bounds }
        val empty = elementBounds.isEmpty()

        when (direction) {
            Direction.DOWN -> {
                currentX = initialX()
                elementWidth = initialWidth()
                elementHeight = elementHeight.coerceIn(minHeight, maxHeight)
                currentY = if (empty) {
                    initialY()
                } else {
                    elementBounds.maxOf { it.y + it.height } + spacing
                }
            }

            Direction.UP -> {
                currentX = initialX()
                elementWidth = initialWidth()
                elementHeight = minHeight
                val bottomEdge = if (empty) {
                    widget.bounds.y + widget.bounds.height - paddingY - marginY
                } else {
                    elementBounds.minOf { it.y } - spacing
                }
                currentY = bottomEdge - elementHeight
            }

            Direction.RIGHT -> {
                currentY = initialY()
                elementHeight = initialHeight()
                elementWidth = minWidth
                currentX = if (empty) {
                    initialX()
                } else {
                    elementBounds.maxOf { it.x + it.width } + spacing
                }
            }

            Direction.LEFT -> {
                currentY = initialY()
                elementHeight = initialHeight()
                elementWidth = minWidth
                val rightEdge = if (empty) {
                    widget.bounds.x + widget.bounds.width - paddingX - marginX
                } else {
                    elementBounds.minOf { it.x } - spacing
                }
                currentX = rightEdge - elementWidth
            }
        }
    }

    fun <T> append(direction: Direction, spacing: Double = 0.0, block: WidgetBuilder.() -> T): T {
        step(direction, spacing)
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

    var marginX by widget.style::marginX
    var marginY by widget.style::marginY
    var margin: Vector2
        get() = Vector2(marginX, marginY)
        set(value) {
            marginX = value.x
            marginY = value.y
        }

    var paddingX by widget.style::paddingX
    var paddingY by widget.style::paddingY
    var padding: Vector2
        get() = Vector2(paddingX, paddingY)
        set(value) {
            paddingX = value.x
            paddingY = value.y
        }

    var minWidth by widget.style::minWidth
    var maxWidth by widget.style::maxWidth

    var minHeight by widget.style::minHeight
    var maxHeight by widget.style::maxHeight

    var fontFamily by widget.style::fontFamily
    var fontSize by widget.style::fontSize
    var textColor by widget.style::textColor

    var backgroundColor by widget.style::backgroundColor
    var hoverColor by widget.style::hoverColor
    var focusColor by widget.style::focusColor
    var selectColor by widget.style::selectColor
    var strokeColor by widget.style::strokeColor

    var strokeWeight by widget.style::strokeWeight
    var borderRadius by widget.style::borderRadius
}

fun widget(
    bounds: Rectangle,
    zIndex: Int = 0,
    build: WidgetBuilder.() -> Unit = {},
): Widget {
    val builder = WidgetBuilder(bounds, zIndex)
    builder.build()
    val widget = builder.widget
    registerWidget(widget)
    return widget
}

fun WidgetBuilder.widget(
    bounds: Rectangle,
    zIndex: Int = 0,
    build: WidgetBuilder.() -> Unit = {},
): Widget {
    val childBuilder = WidgetBuilder(bounds, zIndex, this.widget)
    childBuilder.build()
    val child = childBuilder.widget
    this.widget.widgets.add(child)
    return child
}
