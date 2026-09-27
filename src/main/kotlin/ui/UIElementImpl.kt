package ui

import org.openrndr.CharacterEvent
import org.openrndr.DropEvent
import org.openrndr.KeyEvent
import org.openrndr.MouseEvent
import org.openrndr.animatable.Animatable
import org.openrndr.draw.Drawer
import org.openrndr.events.Event
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import style.Colors
import widgets.Widget

open class UIElementImpl(
    override var label: String = "",
    override var bounds: Rectangle,
    override var zIndex: Int = 0,
    override var parent: Widget? = null
): UIElement {
    override val description: String? = null

    override val boundsCopy = bounds.copy()

    override val xOffset: Double
        get() = parent?.effectiveXOffset ?: 0.0
    override val yOffset: Double
        get() = parent?.effectiveYOffset ?: 0.0

    val parentBounds: Rectangle
        get() = parent?.bounds ?: bounds

    override var visible = true
    override var interactable = true

    override var visibleIf = { true }
    override var interactableIf = { true }

    override var idleColor = Colors.CLICKABLE
    override var hoverColor = Colors.HOVERED
    override var focusColor = Colors.FOCUSED
    override var selectColor = focusColor

    override var animations = listOf<UIAnimatable>()

    override var fixed = false

    override var acceptsDrop = false
    override var acceptsText = false

    override var isHovered = false
    override var isFocused = false
        set(value) {
            val old = field
            field = value
            if (!old && value) focused.trigger(true)
            else if (old && !value) focused.trigger(false)
        }
    override var isSelected = false
    override var isDragged = false

    override val hovered = Event<Boolean>("ui-element-hovered--$label")
    override val focused = Event<Boolean>("ui-element-focused--$label")
    override val clicked = Event<MouseEvent>("ui-element-clicked--$label")
    override val dropped = Event<DropEvent>("ui-element-dropped--$label")
    override val keyDown = Event<KeyEvent>("ui-element-keyDown--$label")
    override val character = Event<CharacterEvent>("ui-element-character--$label")

    override val position: Vector2
        get() = error("dont use this")

    override val buttonDown = Event<MouseEvent>("ui-element-button-down-$label")
    override val buttonUp = Event<MouseEvent>("ui-element-button-up-$label")
    override val dragged = Event<MouseEvent>("ui-element-dragged-$label")
    override val moved = Event<MouseEvent>("ui-element-moved-$label")
    override val scrolled = Event<MouseEvent>("ui-element-scrolled-$label")
    override val entered = Event<MouseEvent>("ui-element-entered-$label")
    override val exited = Event<MouseEvent>("ui-element-exited-$label")

    override var beforeDraw: UIElement.() -> Unit = {}
    override var afterDraw: UIElement.() -> Unit = {}

    override fun draw(drawer: Drawer) {
        beforeDraw()
        visible = visibleIf()
        if (!visible) {
            isFocused = false
            return
        }
    }
}

