package elements

import org.openrndr.MouseEvent
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import ui.UIElement
import ui.makeDraggable

open class Node(
    bounds: Rectangle,
    var a: Node? = null,
    var b: Node? = null,
): UIElement(bounds) {

    var onButtonDown: (MouseEvent) -> Unit = {}
    var onDrag: (MouseEvent) -> Unit = {}
    var onButtonUp: (MouseEvent) -> Unit = {}

    init {
        makeDraggable(
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
    var radius = bounds.width / 2.0
        set(value) {
            bounds = Rectangle.fromCenter(bounds.center, value * 2.0)
            field = value
        }

    init {
        configure()
    }

    override fun draw(drawer: Drawer) {
        drawer.fill = if (isSelected) {
            ColorRGBa.WHITE.opacify(0.85)
        } else null

        drawer.stroke = effectiveStyle.backgroundColor
        drawer.circle(position, radius)

        drawer.stroke = ColorRGBa.WHITE
        drawer.strokeWeight = 0.01
        a?.let { drawer.lineSegment(it.position, position) }
        b?.let { drawer.lineSegment(it.position, position) }
    }
}
