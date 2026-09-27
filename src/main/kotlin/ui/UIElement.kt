package ui

import org.openrndr.CharacterEvent
import org.openrndr.DropEvent
import org.openrndr.KeyEvent
import org.openrndr.MouseEvent
import org.openrndr.MouseEvents
import org.openrndr.draw.Drawer
import org.openrndr.events.Event
import org.openrndr.shape.Rectangle
import widgets.Widget

/**
 *  A UIElement is a UI primitive that encapsulates
 *  all the necessary logic and flags that a ui element
 *  implementation might need.
 */

interface UIElement: MouseEvents {
    var parent: Widget?

    val label: String
    val description: String?

    var bounds: Rectangle

    var visible: Boolean
    var interactable: Boolean

    var visibleIf: () -> Boolean
    var interactableIf: () -> Boolean

    var fixed: Boolean

    var acceptsDrop: Boolean
    var acceptsText: Boolean

    var isHovered: Boolean
    var isFocused: Boolean
    var isSelected: Boolean
    var isDragged: Boolean

    val hovered: Event<Boolean>
    val focused: Event<Boolean>
    val clicked: Event<MouseEvent>
    val dropped: Event<DropEvent>
    val keyDown: Event<KeyEvent>
    val character: Event<CharacterEvent>

    val yOffset: Double
    var zIndex: Int

    var beforeDraw: UIElement.() -> Unit
    var afterDraw: UIElement.() -> Unit

    fun draw(drawer: Drawer)
}