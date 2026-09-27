package widgets

import UIHost
import deregisterWidget
import org.openrndr.draw.isolated
import ui.close
import kotlin.collections.sortedBy

fun Widget.close() {
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
        if(clip) drawer.drawStyle.clip = this@draw.bounds

        drawer.stroke = this@draw.stroke
        drawer.fill = background
        drawer.rectangle(this@draw.bounds)

        val sorted = elements.sortedBy { it.zIndex }

        for (el in sorted) {
            if (!el.isFocused) {
                drawer.isolated {
                    if (!el.fixed && yOffset != 0.0) {
                        drawer.translate(0.0, smoothYoffset)
                    }
                    el.draw(drawer)
                }
            }
        }

        for (el in sorted) {
            if (el.isFocused) {
                drawer.isolated {
                    if (!el.fixed && yOffset != 0.0) {
                        drawer.translate(0.0, smoothYoffset)
                    }
                    el.draw(drawer)
                }
            }
        }

        drawer.drawStyle.clip = null
    }
}