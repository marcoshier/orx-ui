package elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.shapes.primitives.regularPolygon
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.panel.collections.SelectableList
import org.openrndr.panel.collections.SelectableMutableList
import org.openrndr.shape.Rectangle
import style.Colors
import style.Style
import style.styleProperty
import transformPos
import ui.UIElement
import ui.applyBoundsFromText
import ui.mousePosition
import ui.registerElement
import widget.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

open class Dropdown<T> (
    var getter: () -> T,
    val setter: (T) -> Unit,
    bounds: Rectangle? = null,
    configure: Dropdown<T>.() -> Unit = {},
): UIElement(bounds ?: Rectangle(10.0, 10.0, 60.0, 30.0)) {

    constructor(
        current: KMutableProperty0<T>,
        bounds: Rectangle? = null,
        configure: Dropdown<T>.() -> Unit = {}
    ): this({ current.get() }, { current.set(it) }, bounds, configure)

    constructor(
        obj: Any,
        propertyRef: KMutableProperty1<Any, T>,
        bounds: Rectangle? = null,
        configure: Dropdown<T>.() -> Unit = {}
    ): this({ propertyRef.get(obj) }, { propertyRef.set(obj, it) }, bounds, configure)

    var entries: () -> List<T> = { listOf() }
    var entryName: (T) -> String = { "" }

    var onSelection = {}

    var textAlign = Vector2(0.0, 0.5)

    var collapsedBounds = this.bounds.copy()

    var open = false
        set(value) {
            field = value
            bounds = if (value)
                collapsedBounds.copy(height = collapsedBounds.height * entries().size)
            else
                collapsedBounds.copy()
        }

    fun findTarget(localPos: Vector2): Int {
        if (!isHovered) return -1
        val y = localPos.y - collapsedBounds.y
        return (y / collapsedBounds.height).toInt()
    }

    init {

        focused.listen { focused ->
            if (!focused) { open = false }
        }

        buttonDown.listen {
            it.cancelPropagation()
            if (!open) {
                open = true
                return@listen
            }
            val idx = ((it.position.y - collapsedBounds.y) / collapsedBounds.height).toInt()
            if (idx > 0) {
                val others = entries() - getter()
                setter(others[(idx - 1).coerceIn(0, others.lastIndex)])
                onSelection()
            }
            open = false
        }

        if (bounds == null) {
            val text = entries().firstOrNull()?.let { entryName(it) } ?: "XXX"
            applyBoundsFromText(text)
            collapsedBounds = this.bounds.copy()
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible()) return

        val list = entries()
        val current = getter()
        val currentName = entryName(current)

        val boxHeight = if (open) collapsedBounds.height * list.size else collapsedBounds.height
        val box = Rectangle(collapsedBounds.x, collapsedBounds.y, collapsedBounds.width, boxHeight)
        val target = findTarget(transformPos(mousePosition(), this))

        drawer.fontMap = effectiveStyle.fontMap

        if (label.isNotEmpty()) {
            drawer.fill = effectiveStyle.textColor
            drawer.stroke = null
            drawer.writer {
                this.box = collapsedBounds
                horizontalAlign = textAlign.x
                verticalAlign = textAlign.y
                text(label.uppercase())
            }
        }

        drawer.stroke = null
        drawer.fill = effectiveStyle.backgroundColor
        drawer.roundedRectangle(box.toRounded(2.0))

        drawer.fill = ColorRGBa.BLACK
        drawer.writer {
            this.box = Rectangle(bounds.x + 15.0, bounds.y, bounds.width, collapsedBounds.height)
            horizontalAlign = 0.0
            verticalAlign = 0.5
            text(currentName)
            cursor.x
        }


        if (open) {
            for ((j, entry) in (list - current).withIndex()) {
                val r = Rectangle(
                    bounds.x,
                    bounds.y + j * collapsedBounds.height + collapsedBounds.height,
                    bounds.width,
                    collapsedBounds.height
                )

                drawer.stroke = null
                drawer.fill = if (target == (j + 1)) effectiveStyle.dropdownEntryFill else effectiveStyle.backgroundColor
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

        var phase = if (open) 30.0 else 210.0

        drawer.fill = Colors.HOVERED.shade(0.5)
        val rect = Rectangle(bounds.x + bounds.width - collapsedBounds.height, bounds.y, collapsedBounds.height, collapsedBounds.height)
        if (list.size == 1) {
            drawer.fill = drawer.fill?.opacify(0.5)
            phase = 210.0
        }
        drawer.contour(regularPolygon(3, rect.center, 5.0, phase = phase))

    }
}


var Style.dropdownEntryFill: ColorRGBa? by styleProperty(Colors.CLICKABLE)


inline fun <reified T: Enum<T>> dropdown(
    current: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    crossinline configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(current, bounds) {
        entries = { enumValues<T>().toList() }
        entryName = { it.name }
        configure()
    }
    registerElement(b)
    return b
}

@JvmName("dropdownSelectableList0")
fun <T : Any> dropdown(
    listRef: KMutableProperty0<SelectableList<T>>,
    bounds: Rectangle? = null,
    entryName: (T) -> String = { it.toString() },
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(
        getter = { listRef.get().let { (it.selected ?: it.firstOrNull()) as T } },
        setter = { s -> listRef.get().let { it.selectedIndex = it.indexOf(s).coerceAtLeast(0) } },
        bounds
    ) {
        entries = { listRef.get().toList() }
        this.entryName = entryName
        configure()
    }
    registerElement(b)
    return b
}

@JvmName("dropdownSelectableMutableList0")
fun <T : Any> dropdown(
    listRef: KMutableProperty0<SelectableMutableList<T>>,
    bounds: Rectangle? = null,
    entryName: (T) -> String = { it.toString() },
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(
        getter = { listRef.get().let { (it.selected ?: it.firstOrNull()) as T } },
        setter = { s -> listRef.get().let { it.selectedIndex = it.indexOf(s).coerceAtLeast(0) } },
        bounds
    ) {
        entries = { listRef.get().toList() }
        this.entryName = entryName
        configure()
    }
    listRef.get().changed.listen {
        if (it.isNotEmpty() && it.selectedIndex == -1) it.selectedIndex = 0
    }
    registerElement(b)
    return b
}

inline fun <reified T: Enum<T>> WidgetBuilder.dropdown(
    current: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    crossinline configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(current, bounds) {
        entries = { enumValues<T>().toList() }
        entryName = { it.name }
        configure()
    }
    add(b)
    return b
}

@Suppress("UNCHECKED_CAST")
fun WidgetBuilder.dropdown(
    obj: Any,
    current: KMutableProperty1<Any, Enum<*>>,
    configure: Dropdown<Enum<*>>.() -> Unit = {},
): Dropdown<Enum<*>> {
    val b = Dropdown( obj, current, currentElementBounds) {
        val kClass = current.returnType.classifier as kotlin.reflect.KClass<*>
        val constants = kClass.java.enumConstants as Array<Enum<*>>

        entries = { constants.toList() }
        entryName = { it.name }
        configure()
    }
    add(b)
    return b
}

@JvmName("dropdownWidgetSelectableList0")
fun <T : Any> WidgetBuilder.dropdown(
    listRef: KMutableProperty0<SelectableList<T>>,
    entryName: (T) -> String = { it.toString() },
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(
        getter = { listRef.get().let { (it.selected ?: it.firstOrNull()) as T } },
        setter = { s -> listRef.get().let { it.selectedIndex = it.indexOf(s).coerceAtLeast(0) } },
        currentElementBounds
    ) {
        entries = { listRef.get().toList() }
        this.entryName = entryName
        configure()
    }
    add(b)
    return b
}

@JvmName("dropdownWidgetSelectableList1")
fun <T : Any> WidgetBuilder.dropdown(
    obj: Any,
    listRef: KMutableProperty1<Any, SelectableList<T>>,
    entryName: (T) -> String = { it.toString() },
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(
        getter = { listRef.get(obj).let { (it.selected ?: it.firstOrNull()) as T } },
        setter = { s -> listRef.get(obj).let { it.selectedIndex = it.indexOf(s).coerceAtLeast(0) } },
        currentElementBounds
    ) {
        entries = { listRef.get(obj).toList() }
        this.entryName = entryName
        configure()
    }
    add(b)
    return b
}


@JvmName("dropdownWidgetSelectableMutableList0")
fun <T : Any> WidgetBuilder.dropdown(
    listRef: KMutableProperty0<SelectableMutableList<T>>,
    entryName: (T) -> String = { it.toString() },
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(
        getter = { listRef.get().let { (it.selected ?: it.firstOrNull()) as T } },
        setter = { s -> listRef.get().let { it.selectedIndex = it.indexOf(s).coerceAtLeast(0) } },
        currentElementBounds
    ) {
        entries = { listRef.get().toList() }
        this.entryName = entryName
        configure()
    }
    listRef.get().changed.listen {
        if (it.isNotEmpty() && it.selectedIndex == -1) it.selectedIndex = 0
    }
    add(b)
    return b
}

@JvmName("dropdownWidgetSelectableMutableList1")
fun <T : Any> WidgetBuilder.dropdown(
    obj: Any,
    listRef: KMutableProperty1<Any, SelectableMutableList<T>>,
    entryName: (T) -> String = { it.toString() },
    configure: Dropdown<T>.() -> Unit = {},
): Dropdown<T> {
    val b = Dropdown(
        getter = { listRef.get(obj).let { (it.selected ?: it.firstOrNull()) as T } },
        setter = { s -> listRef.get(obj).let { it.selectedIndex = it.indexOf(s).coerceAtLeast(0) } },
        currentElementBounds
    ) {
        entries = { listRef.get(obj).toList() }
        this.entryName = entryName
        configure()
    }
    listRef.get(obj).changed.listen {
        if (it.isNotEmpty() && it.selectedIndex == -1) it.selectedIndex = 0
    }
    add(b)
    return b
}
