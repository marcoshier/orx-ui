package widget

import lib.smoothing
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.Style
import ui.UIElement
import ui.getUniqueId
import ui.updateAnimations

open class Widget(
    var bounds: Rectangle,
    var zIndex: Int = 0,
    var parent: Widget? = null
) {

    val id = "#${getId()}-${getUniqueId()}"
    var selectors = setOf(id)

    open var label = selectors.first()

    open val elements = mutableListOf<UIElement>()
    open val widgets = mutableListOf<Widget>()

    var style: Style = parent?.style?.copy() ?: Style().apply {
        backgroundColor = null
    }

    val contentBounds: Rectangle
        get() = (elements.map { it.bounds } + widgets.map { it.bounds }).bounds

    var clip = true

    var scrollSmoothing = 0.75

    val effectiveYOffset: Double
        get() = (parent?.effectiveYOffset ?: minYOffset) + smoothYoffset
    val effectiveXOffset: Double
        get() = (parent?.effectiveXOffset ?: minXOffset) + smoothXoffset

    open val xScrollable: Boolean
        get() = contentBounds.width > bounds.width
    open val yScrollable: Boolean
        get() = contentBounds.height > bounds.height

    var minXOffset: Double = 0.0
    var minYOffset: Double = 0.0
    val maxXOffset: Double
        get() {
            val overflow = (contentBounds.width + style.paddingX) - bounds.width
            return if (overflow > 0) -overflow else minXOffset
        }
    val maxYOffset: Double
        get() {
            val overflow = contentBounds.height + style.paddingY * 2 - bounds.height
            return if (overflow > 0) -overflow else minYOffset
        }

    var xOffset = minXOffset
        set(value) { field = value.coerceIn(maxXOffset, minXOffset) }
    var yOffset = minYOffset
        set(value) { field = value.coerceIn(maxYOffset, minYOffset) }

    val smoothXoffset by smoothing(::xOffset, scrollSmoothing)
    val smoothYoffset by smoothing(::yOffset, scrollSmoothing)


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

            val (fixed, scrolling) = elements.sortedBy { it.zIndex }.partition { it.fixed }

            fun drawElement(el: UIElement) {
                if (el === focusElement) return
                drawer.isolated {
                    if (!el.fixed) drawer.translate(smoothXoffset, smoothYoffset)
                    el.beforeDraw(el)
                    el.updateAnimations()
                    el.draw(drawer)
                    el.afterDraw(el)
                }
            }

            scrolling.forEach(::drawElement)

            for (child in widgets.sortedBy { it.zIndex }) {
                drawer.isolated {
                    drawer.translate(smoothXoffset, smoothYoffset)
                    child.draw(drawer, focusElement)
                }
            }

            fixed.forEach(::drawElement)

            drawer.drawStyle.clip = null
        }
    }

}


fun Widget.getId() = this::class.simpleName?.lowercase()