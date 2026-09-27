package widgets

import lib.smoothing
import org.openrndr.events.Event
import ui.UIElement
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.WidgetStyle

open class WidgetImpl(
    override var label: String = "",
    override var bounds: Rectangle,
    override var zIndex: Int = 0,
    override var parent: Widget? = null
) : Widget {

    override val elements = mutableListOf<UIElement>()
    override val widgets = mutableListOf<Widget>()

    override var style = parent?.style?.copy() ?: WidgetStyle()

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

}
