package widget

import ancestors
import deregisterWidget
import org.openrndr.extra.shapes.primitives.intersection
import org.openrndr.math.Vector2
import org.openrndr.math.sum
import org.openrndr.shape.Rectangle
import ui.close

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

fun Widget.screenBounds(): Rectangle {
    val px = parent?.effectiveXOffset ?: 0.0
    val py = parent?.effectiveYOffset ?: 0.0
    val pd = ancestors().map { it.dragOffset }.sum() + dragOffset
    return bounds.movedBy(Vector2(px, py) + pd)
}

fun Widget.clipBounds(): Rectangle {
    var clip = screenBounds()
    var a = parent
    while (a != null) {
        clip = clip.intersection(a.screenBounds())
        a = a.parent
    }
    return clip
}