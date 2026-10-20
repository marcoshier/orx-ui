package widget

import lib.smoothing
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import org.openrndr.math.Vector2
import ui.UIElement
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.Style
import ui.updateAnimations

open class Widget(
    var bounds: Rectangle,
    var zIndex: Int = 0,
    var parent: Widget? = null
) {

    // TODO
    var selectors = listOf(id())
    open var label = selectors.first()
    val description: String? = null

    open val elements = mutableListOf<UIElement>()
    val widgets = mutableListOf<Widget>()

    var style: Style = parent?.style?.copy() ?: UIHost.current.style.default.copy().apply {
        backgroundColor = ColorRGBa.TRANSPARENT
    }

    val contentBounds: Rectangle
        get() = (elements.map { it.bounds } + widgets.map { it.bounds }).bounds

    var clip = true

    var scrollSmoothing = 0.75

    val effectiveYOffset: Double
        get() = (parent?.effectiveYOffset ?: 0.0) + smoothYoffset
    val effectiveXOffset: Double
        get() = (parent?.effectiveXOffset ?: 0.0) + smoothXoffset

    var xOffset = 0.0
        set(value) { field = value.coerceIn(maxXOffset, 0.0) }
    var yOffset = 0.0
        set(value) { field = value.coerceIn(maxYOffset, 0.0) }

    val smoothXoffset by smoothing(::xOffset, scrollSmoothing)
    val smoothYoffset by smoothing(::yOffset, scrollSmoothing)

    open val xScrollable: Boolean
        get() = contentBounds.width > bounds.width
    open val yScrollable: Boolean
        get() = contentBounds.height > bounds.height

    val maxXOffset: Double
        get() {
            val overflow = (contentBounds.width + style.paddingX) - bounds.width
            return if (overflow > 0) -overflow else 0.0
        }
    val maxYOffset: Double
        get() {
            val overflow = contentBounds.height + style.paddingY * 2 - bounds.height
            return if (overflow > 0) -overflow else 0.0
        }

    var dragOffset = Vector2.ZERO

    val closed = Event<Unit>("widget-closed--$label")

    open fun draw(drawer: Drawer, focusElement: UIElement? = null) {
        drawer.isolated {
            if (clip) {
                drawer.drawStyle.clip = this@Widget.clipBounds()
            }

            drawer.translate(dragOffset)

            drawer.stroke = style.strokeColor
            drawer.fill = style.backgroundColor
            drawer.rectangle(this@Widget.bounds)

            val sorted = elements.sortedBy { it.zIndex }

            for (el in sorted) {
                if (el === focusElement) continue

                drawer.isolated {
                    if (!el.fixed) {
                        drawer.translate(smoothXoffset, smoothYoffset)
                    }
                    el.beforeDraw(el)
                    el.updateAnimations()
                    el.draw(drawer)
                    el.afterDraw(el)
                }
            }

            for (child in widgets.sortedBy { it.zIndex }) {
                drawer.isolated {
                    drawer.translate(smoothXoffset, smoothYoffset)
                    child.draw(drawer, focusElement)
                }
            }

            drawer.drawStyle.clip = null
        }
    }

}


fun Widget.id(): String {
    // to finish
    return this::class.simpleName?.lowercase() ?: ""
}
