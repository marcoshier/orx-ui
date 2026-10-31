package elements

import org.openrndr.MouseEvent
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import style.Colors
import ui.UIElement
import ui.setupDraggable

open class Node(
    bounds: Rectangle,
    var a: Node? = null,
    var b: Node? = null,
): UIElement(bounds) {

    var onButtonDown: (MouseEvent) -> Unit = {}
    var onDrag: (MouseEvent) -> Unit = {}
    var onButtonUp: (MouseEvent) -> Unit = {}

    init {
        setupDraggable(
            onButtonDown = { onButtonDown(it) },
            onDragged = { onDrag(it) },
            onButtonUp = { onButtonUp(it) }
        )
    }
}

class HandleNode(
    initialPosition: Vector2,
    a: Node? = null, b: Node? = null,
    configure: HandleNode.() -> Unit = {}
): Node(
    Rectangle.fromCenter(initialPosition, 17.0),
    a, b
) {

    init {
        configure()
    }

    val radius: Double
        get() = bounds.width / 2.0

    override fun draw(drawer: Drawer) {
        drawer.fill = if (isSelected) {
            style.selectColor
        } else null

        drawer.stroke = when {
            isHovered -> style.hoverColor
            isDragged -> style.selectColor
            else -> Colors.CLICKABLE
        }
        drawer.circle(position, radius / 2.0)

        drawer.stroke = ColorRGBa.WHITE
        drawer.strokeWeight = 0.01
        a?.let { drawer.lineSegment(it.position, position) }
        b?.let { drawer.lineSegment(it.position, position) }
    }
}
