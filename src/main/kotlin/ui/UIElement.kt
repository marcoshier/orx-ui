package ui

import org.openrndr.CharacterEvent
import org.openrndr.DropEvent
import org.openrndr.KeyEvent
import org.openrndr.MouseEvent
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.*
import widget.Widget

open class UIElement(
    var bounds: Rectangle,
    open var zIndex: Int = 0,
    var parent: Widget? = null
) {

    val id = "#${getId()}-${getUniqueId()}"
    var selectors = setOf("${getId()}")

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
    var isActive = false
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

    private var cachedStyle: Style? = null
    private var cachedFrame = -1
    private var cachedStates: Set<StyleModifier> = emptySet()
    private var cachedSelectors: Set<String> = emptySet()

    val effectiveStyle: Style
        get() {
            val frame = UIHost.current.program.frameCount
            val states = activeStates()

            cachedStyle?.let {
                if (frame == cachedFrame && states == cachedStates && selectors === cachedSelectors) {
                    return it
                }
            }

            val resolvedStyle = UIHost.current.styleStack.resolve(styleBases(), states, style)
            val s = updateTransitions(resolvedStyle)

            cachedStyle = s
            cachedFrame = frame
            cachedStates = states
            cachedSelectors = selectors

            return s
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

        for ((name, to) in target.extras) {
            when (to) {
                is Double -> {
                    val tw = doubleTransitions.getOrPut(name) { DoubleTransition(to, to, now, null) }

                    if (tw.to != to) {
                        doubleTransitions[name] = DoubleTransition(tw.valueAt(now), to, now, target.transitions[name])
                    }

                    if (!doubleTransitions[name]!!.completed(now)) {
                        animating = true
                    }
                }
                is ColorRGBa -> {
                    val tw = colorsTransitions.getOrPut(name) { ColorRGBaTransition(to, to, now, null) }

                    if (tw.to != to) {
                        colorsTransitions[name] = ColorRGBaTransition(tw.valueAt(now), to, now, target.transitions[name])
                    }

                    if (!colorsTransitions[name]!!.completed(now)) {
                        animating = true
                    }
                }
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
        for ((name, v) in target.extras) {
            when (v) {
                is Double -> c.extras[name] = doubleTransitions[name]!!.valueAt(now)
                is ColorRGBa -> c.extras[name] = colorsTransitions[name]!!.valueAt(now)
            }
        }

        return c
    }

    private val typeSelectors: List<String> by lazy {
        generateSequence<Class<*>>(this::class.java) { it.superclass }
            .takeWhile { it != UIElement::class.java }
            .map { it.simpleName.lowercase() }
            .filter { it.isNotEmpty() }
            .toList()
    }

    private fun styleBases(): Set<String> = selectors + id + typeSelectors + "*"

    private fun activeStates(): Set<StyleModifier> = buildSet {
        if (isHovered) add(StyleModifier.HOVER)
        if (isActive) add(StyleModifier.ACTIVE)
        if (isFocused) add(StyleModifier.FOCUS)
        if (isSelected) add(StyleModifier.SELECTED)
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
        drawer.fontMap = effectiveStyle.fontMap
        this@applyBoundsFromText.bounds = writer {
            val h = fontMap!!.ascenderLength * 1.25
            box = Rectangle(position, 9999.0, h)
            text(label, false)
            glyphOutput.rectangles.map { it.second }.bounds.offsetEdges(
                textWidth(text) * 0.5,
                h
            ).movedTo(position)
        }
    }
}