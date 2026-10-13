package widgets

import UIHost
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import ui.UIElement
import org.openrndr.events.Event
import org.openrndr.shape.Rectangle
import style.Style

/**
 * A widget provides a container for multiple ui.UIElement instances.
 *  It takes care of displaying them with a clip mask, scrolling
 *  local z-indexing and cleanup.
 */

interface Widget {
    val elements: List<UIElement>
    val widgets: List<Widget>
    var parent: Widget?

    var style: Style

    val selectors: List<String>
    val label: String
    val description: String?

    var bounds: Rectangle
    val contentBounds: Rectangle

    var clip: Boolean

    val effectiveXOffset: Double
    val effectiveYOffset: Double

    var xOffset: Double
    var yOffset: Double

    val smoothXoffset: Double
    val smoothYoffset: Double

    val maxXOffset: Double
    val maxYOffset: Double

    val xScrollable: Boolean
    val yScrollable: Boolean

    val closed: Event<Unit>

    var zIndex: Int

    fun draw(drawer: Drawer)
}