package widgets

import UIHost
import lib.smoothing
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import ui.UIElement
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.Style
import ui.updateAnimations

open class WidgetImpl(
    override var bounds: Rectangle,
    override var zIndex: Int = 0,
    override var parent: Widget? = null
) : Widget {

    override var selectors = listOf(id())
    override var label = selectors.first()
    override val description: String? = null

    override val elements = mutableListOf<UIElement>()
    override val widgets = mutableListOf<Widget>()

    override var style = parent?.style?.copy() ?: Style(backgroundColor = ColorRGBa.TRANSPARENT)

    override val contentBounds: Rectangle
        get() = (elements.map { it.bounds } + widgets.map { it.bounds }).bounds

    override var clip = true

    var scrollSmoothing = 0.75

    override val effectiveYOffset: Double
        get() = (parent?.effectiveYOffset ?: 0.0) + smoothYoffset
    override val effectiveXOffset: Double
        get() = (parent?.effectiveXOffset ?: 0.0) + smoothXoffset

    override var xOffset = 0.0
        set(value) { field = value.coerceIn(maxXOffset, 0.0) }
    override var yOffset = 0.0
        set(value) { field = value.coerceIn(maxYOffset, 0.0) }

    override val smoothXoffset by smoothing(::xOffset, scrollSmoothing)
    override val smoothYoffset by smoothing(::yOffset, scrollSmoothing)

    override val xScrollable: Boolean
        get() = contentBounds.width > bounds.width
    override val yScrollable: Boolean
        get() = contentBounds.height > bounds.height

    override val maxXOffset: Double
        get() {
            val overflow = (contentBounds.width + style.paddingX) - bounds.width
            return if (overflow > 0) -overflow else 0.0
        }
    override val maxYOffset: Double
        get() {
            val overflow = contentBounds.height + style.paddingY * 2 - bounds.height
            return if (overflow > 0) -overflow else 0.0
        }

    override val closed = Event<Unit>("widget-closed--$label")

    override fun draw(drawer: Drawer) {
            drawer.isolated {
                if (clip) {
                    drawer.drawStyle.clip = this@WidgetImpl.clipBounds()
                }

                drawer.stroke = style.strokeColor
                drawer.fill = style.backgroundColor
                drawer.rectangle(this@WidgetImpl.bounds)

                val sorted = elements.sortedBy { it.zIndex }

                for (el in sorted) {
                    drawer.isolated {
                        if (!el.fixed) drawer.translate(smoothXoffset, smoothYoffset)
                        el.updateAnimations()
                        el.draw(drawer)
                    }
                }

                for (child in widgets.sortedBy { it.zIndex }) {
                    drawer.isolated {
                        drawer.translate(smoothXoffset, smoothYoffset)
                        child.draw(drawer)
                    }
                }

                drawer.drawStyle.clip = null
            }
        }

}


fun WidgetImpl.id(): String {
    // to finish
    return this::class.simpleName?.lowercase() ?: ""
}
