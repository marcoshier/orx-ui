package elements

import mousePosition
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.shapes.primitives.regularPolygon
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import registerElement
import style.Colors
import style.Fonts
import transformPos
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

open class Dropdown<T> (
    label: String,
    bounds: Rectangle,
    var getter: () -> T,
    val setter: (T) -> Unit,
    configure: Dropdown<T>.() -> Unit = {},
): UIElementImpl(label, bounds) {

    constructor(
        label: String,
        bounds: Rectangle,
        current: KMutableProperty0<T>,
        configure: Dropdown<T>.() -> Unit = {}
    ): this(label, bounds, { current.get() }, { current.set(it) }, configure)

    constructor(
        label: String,
        bounds: Rectangle,
        obj: Any,
        propertyRef: KMutableProperty1<Any, T>,
        configure: Dropdown<T>.() -> Unit = {}
    ): this(label, bounds, { propertyRef.get(obj) }, { propertyRef.set(obj, it) }, configure)

    var entries: () -> List<T> = { listOf() }
    var entryName: (T) -> String = { "" }

    var onSelection = {}

    var textAlign = Vector2(0.0, 0.5)

    val collapsedBounds = bounds.copy()

    fun findTarget(localPos: Vector2): Int {
        if (!isHovered) return -1
        val y = localPos.y - collapsedBounds.y
        return (y / collapsedBounds.height).toInt()
    }

    init {
        buttonDown.listen {
            it.cancelPropagation()
        }

        focused.listen { open ->
            this.bounds = if (open)
                collapsedBounds.copy(height = collapsedBounds.height * entries().size)
            else
                collapsedBounds.copy()
        }

        clicked.listen {
            if (!isFocused) {
                isFocused = true
            } else {
                val idx = findTarget(it.position)
                if (idx <= 0) {
                    isFocused = false
                } else {
                    val others = entries() - getter()
                    setter(others[(idx - 1).coerceIn(0, others.size - 1)])
                    onSelection()
                    isFocused = false
                }
            }
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)

        val list = entries()
        val current = getter()
        val currentName = entryName(current)

        val boxHeight = if (isFocused) collapsedBounds.height * list.size else collapsedBounds.height
        val box = Rectangle(collapsedBounds.x, collapsedBounds.y, collapsedBounds.width, boxHeight)
        val target = findTarget(transformPos(mousePosition(), this))

        drawer.fontMap = style.font

        if (label.isNotEmpty()) {
            drawer.fill = style.textColor
            drawer.stroke = null
            drawer.writer {
                this.box = collapsedBounds
                horizontalAlign = textAlign.x
                verticalAlign = textAlign.y
                text(label.uppercase())
            }
        }

        val mainFill = when {
            isFocused -> if (target == 0) style.hoverColor else style.focusColor
            isHovered -> style.hoverColor
            else -> style.idleColor
        }

        drawer.stroke = null
        drawer.fill  = mainFill
        drawer.roundedRectangle(box.toRounded(2.0))

        drawer.fill = ColorRGBa.BLACK
        drawer.writer {
            this.box = Rectangle(bounds.x + 15.0, bounds.y, bounds.width, collapsedBounds.height)
            horizontalAlign = 0.0
            verticalAlign = 0.5
            text(currentName)
            cursor.x
        }


        if (isFocused) {
            for ((j, entry) in (list - current).withIndex()) {
                val r = Rectangle(
                    bounds.x,
                    bounds.y + j * collapsedBounds.height + collapsedBounds.height,
                    bounds.width,
                    collapsedBounds.height
                )

                drawer.stroke = null
                drawer.fill = if (target == (j + 1)) style.hoverColor else style.idleColor
                drawer.rectangle(r)

                drawer.fill = ColorRGBa.BLACK
                drawer.writer {
                    this.box = r.copy(corner = Vector2(r.x + 15.0, r.y))
                    horizontalAlign = 0.0
                    verticalAlign = 0.5
                    text(entryName(entry))
                    cursor.x
                }
            }
        }

        var phase = if (isFocused) 30.0 else 210.0

        drawer.fill = Colors.HOVERED.shade(0.5)
        val rect = Rectangle(bounds.x + bounds.width - collapsedBounds.height, bounds.y, collapsedBounds.height, collapsedBounds.height)
        if (list.size == 1) {
            drawer.fill = drawer.fill?.opacify(0.5)
            phase = 210.0
        }
        drawer.contour(regularPolygon(3, rect.center, 5.0, phase = phase))

    }
}

fun <T> dropdown(
    label: String,
    bounds: Rectangle,
    entries: () -> List<T>,
    entryName: (T) -> String,
    current: KMutableProperty0<T>,
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(label, bounds, current) {
        this.entries = entries
        this.entryName = entryName
        configure()
    }
    registerElement(b)
    return b
}

inline fun <reified T: Enum<T>> dropdown(
    label: String,
    bounds: Rectangle,
    current: KMutableProperty0<T>,
    crossinline configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(label, bounds, current) {
        entries = { enumValues<T>().toList() }
        entryName = { it.name }
        configure()
    }
    registerElement(b)
    return b
}

fun <T> WidgetBuilder.dropdown(
    label: String,
    bounds: Rectangle,
    entries: () -> List<T>,
    entryName: (T) -> String,
    current: KMutableProperty0<T>,
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(label, bounds, current) {
        this.entries = entries
        this.entryName = entryName
        configure()
    }
    add(b)
    return b
}

inline fun <reified T: Enum<T>> WidgetBuilder.dropdown(
    label: String,
    bounds: Rectangle,
    current: KMutableProperty0<T>,
    crossinline configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(label, bounds, current) {
        entries = { enumValues<T>().toList() }
        entryName = { it.name }
        configure()
    }
    add(b)
    return b
}