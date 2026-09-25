package widgets

import org.openrndr.events.Event
import ui.UIElement
import org.openrndr.shape.Rectangle

open class WidgetImpl(
    override var label: String = "",
    override var bounds: Rectangle,
    override var zIndex: Int = 0,
) : Widget {

    override val elements = mutableListOf<UIElement>()

    override var yOffset = 0.0
    override val closed = Event<Unit>("widget-closed--$label")

    fun add(element: UIElement) {
        element.parent = this
        elements.add(element)
    }
}