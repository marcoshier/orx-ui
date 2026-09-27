package widgets

import UIHost
import deregisterWidget
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.intersection
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import ui.close
import ui.updateAnimations
import kotlin.collections.sortedBy

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

context(host: UIHost)
fun Widget.draw() {
    val drawer = host.context.drawer

    drawer.isolated {

        if (clip) {
            drawer.drawStyle.clip = this@draw.clipBounds()
        }

        drawer.stroke = style.stroke
        drawer.fill = style.background
        drawer.rectangle(this@draw.bounds)

        val sorted = elements.sortedBy { it.zIndex }

        for (el in sorted) {
            if (!el.isFocused) {
                drawer.isolated {
                    if (!el.fixed) drawer.translate(smoothXoffset, smoothYoffset)
                    el.updateAnimations()
                    el.draw(drawer)
                }
            }
        }

        for (el in sorted) {
            if (el.isFocused) {
                drawer.isolated {
                    if (!el.fixed) drawer.translate(smoothXoffset, smoothYoffset)
                    el.updateAnimations()
                    el.draw(drawer)
                }
            }
        }

        for (child in widgets.sortedBy { it.zIndex }) {
            drawer.isolated {
                drawer.translate(smoothXoffset, smoothYoffset)
                child.draw()
            }
        }

        drawer.drawStyle.clip = null
    }
}

fun Widget.screenBounds(): Rectangle {
    val px = parent?.effectiveXOffset ?: 0.0
    val py = parent?.effectiveYOffset ?: 0.0
    return bounds.movedBy(Vector2(px, py))
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