package widgets

import org.openrndr.color.ColorRGBa
import ui.UIElement
import org.openrndr.events.Event
import org.openrndr.shape.Rectangle

/**
 * A widget provides a container for multiple ui.UIElement instances.
 *  It takes care of displaying them with a clip mask, scrolling
 *  local z-indexing and cleanup.
 */

interface Widget {
    val elements: List<UIElement>

    var background: ColorRGBa
    var stroke: ColorRGBa

    var label: String

    var bounds: Rectangle

    var clip: Boolean

    var yOffset: Double
    val smoothYoffset: Double
    val maxYOffset: Double

    val closed: Event<Unit>

    var zIndex: Int
}