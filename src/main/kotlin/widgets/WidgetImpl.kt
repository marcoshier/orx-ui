package widgets

import org.openrndr.events.Event
import ui.UIElement
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds

open class WidgetImpl(
    override var label: String = "",
    override var bounds: Rectangle,
    override var zIndex: Int = 0,
) : Widget {

    override val elements = mutableListOf<UIElement>()

    val contentBounds: Rectangle
        get() = elements.map { it.bounds }.bounds

    override var clip = true

    override var yOffset = 0.0
        set(value) {
            field = value.coerceAtLeast(maxYOffset)
        }

    override val maxYOffset: Double
        get() {
            val minY = (elements.minOfOrNull { it.bounds.y } ?: 0.0)
            return bounds.height - (minY - bounds.y) * 2 - contentBounds.height
        }

    override val closed = Event<Unit>("widget-closed--$label")

}