package widget

import ui.deregisterWidget
import org.openrndr.extra.shapes.primitives.intersection
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import ui.UIElement
import ui.UIHost
import ui.ancestors
import ui.close

fun Widget.addElement(element: UIElement) {
    element.parent = this
    elements.add(element)
    UIHost.current.tree.invalidateCache() // TODO smart List
}

fun Widget.addElements(elements: List<UIElement>) {
    elements.forEach { addElement(it) }
}


fun Widget.close() {
    for (widget in widgets) {
        widget.close()
    }

    for (el in elements) {
        el.close()
    }

    closed.trigger(Unit)
    deregisterWidget(this@close)
}

fun Widget.accumulatedOffset(): Vector2 {
    var dx = 0.0
    var dy = 0.0

    ancestors().forEach { w ->
        dx += w.smoothXoffset + w.dragOffset.x
        dy += w.smoothYoffset + w.dragOffset.y
    }

    return Vector2(dx, dy)
}

fun Widget.screenBounds() = bounds.movedBy(accumulatedOffset() + dragOffset)

fun Widget.clipBounds(): Rectangle {
    return ancestors().fold(screenBounds()) { clip, parent ->
        clip.intersection(parent.screenBounds())
    }
}
