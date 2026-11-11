package ui

import org.openrndr.CharacterEvent
import org.openrndr.DropEvent
import org.openrndr.KeyEvent
import org.openrndr.MouseEvent
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.ColorRGBaTransition
import style.DoubleTransition
import style.Style
import style.styleColorProperties
import style.styleDoubleProperties
import widget.Widget

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

    var visible = { true }
    var interactable = { true }

    open var fixed = false

    var acceptsDrop = false
    var acceptsText = false

    var dirty = false

    var precision = 1
        set(value) {
            field = value.coerceIn(1, 4)
        }

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
    var dragPosition: Vector2 = Vector2.ZERO

    val position: Vector2
        get() = bounds.center

    val hovered = Event<Boolean>("ui-element-hovered--$label")
    val focused = Event<Boolean>("ui-element-focused--$label")
    val clicked = Event<MouseEvent>("ui-element-clicked--$label")
    val doubleClicked = Event<MouseEvent>("ui-element-double-clicked--$label")
    val dropped = Event<DropEvent>("ui-element-dropped--$label")
    val keyDown = Event<KeyEvent>("ui-element-keyDown--$label")
    val character = Event<CharacterEvent>("ui-element-character--$label")

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
        val base = selectors + id
        val out = base.toMutableSet()
        if (isHovered) base.forEach { out.add("$it:hover") }
        if (isFocused) base.forEach { out.add("$it:focus") }
        return out
    }

    open fun draw(drawer: Drawer) {
        if (!visible()) {
            isFocused = false
        }
    }
}

fun UIElement.getId() = this::class.simpleName?.lowercase()
fun getUniqueId() = UIHost.current.nextUniqueId()


// todo turn this into a "place" function
fun UIElement.applyBoundsFromText(
    text: String = label,
    position: Vector2 = Vector2(10.0)
) {
    val drawer = UIHost.current.drawer

    drawer.isolated {
        drawer.fontMap = style.font
        this@applyBoundsFromText.bounds = writer {
            val h = fontMap!!.ascenderLength * 3.0
            box = Rectangle(position, 9999.0, h)
            text(label, false)
            glyphOutput.rectangles.map { it.second }.bounds.offsetEdges(
                textWidth(text) * 3.0,
                h
            ).movedTo(position)
        }
    }
}