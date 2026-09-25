package widgets

import UIHost
import deregisterWidget
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import ui.close
import ui.draw
import kotlin.collections.sortedBy

context(host: UIHost)
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
        drawer.drawStyle.clip = bounds

        val sorted = elements.sortedBy { it.zIndex }

        for (el in sorted) if (!el.isFocused) drawer.isolated {
            if (yOffset != 0.0) drawer.translate(0.0, yOffset)
            el.draw(drawer)
        }
        for (el in sorted) if (el.isFocused) drawer.isolated {
            if (yOffset != 0.0) drawer.translate(0.0, yOffset)
            el.draw(drawer)
        }

        drawer.drawStyle.clip = null
    }
}