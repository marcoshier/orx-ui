package ui

import org.openrndr.CharacterEvent
import org.openrndr.DropEvent
import org.openrndr.KeyEvent
import org.openrndr.MouseEvent
import org.openrndr.draw.Drawer
import org.openrndr.events.Event
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import seconds
import style.ColorRGBaTransition
import style.DoubleTransition
import style.Style
import style.styleColorProperties
import style.styleDoubleProperties
import widgets.Widget

open class UIElement(
    var bounds: Rectangle,
    open var zIndex: Int = 0,
    var parent: Widget? = null
) {

    val id = "#${getId()}-${getUniqueId()}"
    var selectors = setOf(id)

    private val activeSelectors: Set<String>
        get() = getCurrentSelectors()

    open var label = id.drop(1)
    var description: String? = null

    val boundsCopy = bounds.copy()

    val xOffset: Double
        get() = parent?.effectiveXOffset ?: 0.0
    val yOffset: Double
        get() = parent?.effectiveYOffset ?: 0.0

    var visible = true
    var interactable = true

    var visibleIf = { true }
    var interactableIf = { true }

    open var fixed = false

    var acceptsDrop = false
    var acceptsText = false

    var dirty = false

    var isHovered = false
        set(value) {
            if (field != value) {
                field = value
                dirty = true
            }
        }
    var isFocused = false
        set(value) {
            val old = field
            field = value
            if (!old && value) focused.trigger(true)
            else if (old && !value) focused.trigger(false)
        }
    var isSelected = false
    var isDragged = false

    val hovered = Event<Boolean>("ui-element-hovered--$label")
    val focused = Event<Boolean>("ui-element-focused--$label")
    val clicked = Event<MouseEvent>("ui-element-clicked--$label")
    val doubleClicked = Event<MouseEvent>("ui-element-double-clicked--$label")
    val dropped = Event<DropEvent>("ui-element-dropped--$label")
    val keyDown = Event<KeyEvent>("ui-element-keyDown--$label")
    val character = Event<CharacterEvent>("ui-element-character--$label")

    val position: Vector2
        get() = error("dont use this")

    val buttonDown = Event<MouseEvent>("ui-element-button-down-$label")
    val buttonUp = Event<MouseEvent>("ui-element-button-up-$label")
    val dragged = Event<MouseEvent>("ui-element-dragged-$label")
    val moved = Event<MouseEvent>("ui-element-moved-$label")
    val scrolled = Event<MouseEvent>("ui-element-scrolled-$label")
    val entered = Event<MouseEvent>("ui-element-entered-$label")
    val exited = Event<MouseEvent>("ui-element-exited-$label")

    var beforeDraw: UIElement.() -> Unit = {}
    var afterDraw: UIElement.() -> Unit = {}

    var animations = listOf<UIAnimatable>()
    val doubleTransitions = mutableMapOf<String, DoubleTransition>()
    val colorsTransitions = mutableMapOf<String, ColorRGBaTransition>()

    val style = Style()
    val effectiveStyle: Style
        get() {
            val target = UIHost.current.style.resolve(activeSelectors, style)
            return updateTransitions(target)
        }

    private fun updateTransitions(style: Style): Style {
        if (style.transitions.isEmpty()) return style

        val target = style
        val now = seconds()

        var animating = false

        for (prop in styleDoubleProperties) {
            val to = prop.get(target)
            val tween = doubleTransitions.getOrPut(prop.name) {
                DoubleTransition(to, to, now, null)
            }

            if (tween.to != to) {
                doubleTransitions[prop.name] = DoubleTransition(
                    tween.valueAt(now), to, now, target.transitions[prop.name]
                )
            }

            if (!doubleTransitions[prop.name]!!.completed(now)) {
                animating = true
            }
        }

        for (prop in styleColorProperties) {
            val to = prop.get(target)
            val tween = colorsTransitions.getOrPut(prop.name) {
                ColorRGBaTransition(to, to, now, null)
            }

            if (tween.to != to) {
                colorsTransitions[prop.name] = ColorRGBaTransition(
                    tween.valueAt(now), to, now, target.transitions[prop.name]
                )
            }

            if (!colorsTransitions[prop.name]!!.completed(now)) {
                animating = true
            }
        }

        if (!animating) {
            return target
        }

        val c = target.copy()

        for (prop in styleDoubleProperties) {
            prop.set(c, doubleTransitions[prop.name]!!.valueAt(now))
        }
        for (prop in styleColorProperties) {
            prop.set(c, colorsTransitions[prop.name]!!.valueAt(now))
        }

        return c
    }

    private fun getCurrentSelectors(): Set<String> {
        val activeList = selectors.toMutableSet()

        if (isHovered) {
            selectors.forEach { activeList.add("$it:hover") }
        }
        if (isFocused) {
            selectors.forEach { activeList.add("$it:focus") }
        }

        return activeList
    }

    open fun draw(drawer: Drawer) {
        visible = visibleIf()
        interactable = interactableIf()
        if (!visible) {
            isFocused = false
        }
    }
}

fun UIElement.getId() = this::class.simpleName?.lowercase()
fun getUniqueId() = UIHost.current.nextUniqueId()
