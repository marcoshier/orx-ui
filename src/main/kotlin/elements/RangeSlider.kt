package elements

import org.openrndr.color.ColorRGBa
import org.openrndr.color.mix
import org.openrndr.draw.Drawer
import org.openrndr.draw.LineCap
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment2D
import org.openrndr.shape.bounds
import ui.registerElement
import style.Colors
import ui.UIElement
import ui.applyBoundsFromText
import widget.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

@Suppress("UNCHECKED_CAST")
open class RangeSlider<T : Comparable<T>>(
    val range: ClosedRange<T>,
    private val getter: () -> ClosedRange<T>,
    private val setter: (ClosedRange<T>) -> Unit,
    bounds: Rectangle? = null,
    configure: RangeSlider<T>.() -> Unit = {}
) : UIElement(bounds ?: Rectangle(10.0, 10.0, 60.0, 30.0)) {

    constructor(
        range: ClosedRange<T>,
        valueRef: KMutableProperty0<ClosedRange<T>>,
        bounds: Rectangle? = null,
        configure: RangeSlider<T>.() -> Unit = {}
    ) : this(range, { valueRef.get() }, { valueRef.set(it) }, bounds, configure)

    constructor(
        range: ClosedRange<T>,
        obj: Any,
        valueRef: KMutableProperty1<Any, ClosedRange<T>>,
        bounds: Rectangle? = null,
        configure: RangeSlider<T>.() -> Unit = {}
    ) : this(range, { valueRef.get(obj) }, { valueRef.set(obj, it) }, bounds, configure)

    val railStart: Vector2
        get() = this.bounds.position(0.05, 0.5)

    val railEnd: Vector2
        get() = this.bounds.position(0.95, 0.5)


    private var activeHandle = -1

    private fun toT(v: T): Double = when (v) {
        is Int -> {
            val r = range as IntRange
            v.toDouble().map(r.first.toDouble(), r.last.toDouble(), 0.0, 1.0, true)
        }
        is Double -> {
            val r = range as ClosedFloatingPointRange<Double>
            v.map(r.start, r.endInclusive, 0.0, 1.0, true)
        }
        else -> 0.0
    }

    private fun fromT(t: Double): T = when (range.start) {
        is Int -> {
            val r = range as IntRange
            t.map(0.0, 1.0, r.first.toDouble(), r.last.toDouble(), true).toInt() as T
        }
        is Double -> {
            val r = range as ClosedFloatingPointRange<Double>
            t.map(0.0, 1.0, r.start, r.endInclusive, true) as T
        }
        else -> error("Unsupported type: ${range.start::class}")
    }

    val tLow: Double get() = toT(getter().start)
    val tHigh: Double get() = toT(getter().endInclusive)

    private fun setHandle(handle: Int, t: Double) {
        val clamped = t.coerceIn(0.0, 1.0)
        val cur = getter()

        val newLow: T
        val newHigh: T

        if (handle == 0) {
            val low = fromT(clamped)
            val high = cur.endInclusive
            newLow = if (low > high) high else low
            newHigh = high
        } else {
            val high = fromT(clamped)
            val low = cur.start
            newLow = low
            newHigh = if (high < low) low else high
        }

        @Suppress("UNCHECKED_CAST")
        val newRange: ClosedRange<T> = when (cur) {
            is ClosedFloatingPointRange<*> -> (newLow as Double).rangeTo(newHigh as Double) as ClosedRange<T>
            is IntRange -> (newLow as Int).rangeTo(newHigh as Int) as ClosedRange<T>
            else -> newLow..newHigh
        }

        setter(newRange)
    }

    private fun tAt(x: Double) = x.map(railStart.x, railEnd.x, 0.0, 1.0, true)

    init {
        var lastX = 0.0
        var dragging = false
        buttonDown.listen {
            it.cancelPropagation()
            val t = tAt(it.position.x)
            activeHandle = if (kotlin.math.abs(t - tLow) <= kotlin.math.abs(t - tHigh)) 0 else 1

            lastX = it.position.x
            dragging = false
        }

        dragged.listen {
            if (activeHandle < 0) return@listen
            dragging = true

            val length = railEnd.x - railStart.x
            val dx = it.position.x - lastX
            lastX = it.position.x

            val friction = precision * 1.0
            val dt = (dx / length) / friction
            val currentT = if (activeHandle == 0) tLow else tHigh

            val v: T = getter().start

            when(v) {
                is Int -> setHandle(activeHandle, tAt(it.position.x))
                is Double -> setHandle(activeHandle, (currentT + dt).coerceIn(0.0, 1.0))
                else -> error("type not supported")
            }
        }

        buttonUp.listen {
            if (!dragging) {
                setHandle(activeHandle, tAt(it.position.x))
            }
            activeHandle = -1
            dragging = false
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible()) return

        val ls = Segment2D(railStart, railEnd)

        if (label.isNotEmpty()) {
            drawer.fill = ColorRGBa.BLACK
            drawer.stroke = null
            drawer.text(label.uppercase(), railStart.x, ls.start.y - 16.0)
        }

        fun drawValue(visible: Boolean): Rectangle {
            return drawer.writer {
                box = Rectangle(railStart.x, ls.start.y - 16.0 - drawer.fontMap!!.height, ls.length, 15.0)
                horizontalAlign = 1.0
                verticalAlign = 0.0
                val v = getter()
                val s = if (v.start is Double) "%.${precision}f".format(v.start) else "${v.start}"
                val e = if (v.endInclusive is Double) "%.${precision}f".format(v.endInclusive) else "${v.endInclusive}"
                text("$s..$e", visible)
                glyphOutput.rectangles.map { it.second }.bounds
            }
        }

        val g = drawValue(false)
        drawer.stroke = null
        drawer.fill = Colors.CLICKABLE.mix(ColorRGBa.WHITE, 0.4)
        drawer.rectangle(g.offsetEdges(1.0))
        drawer.fill = Colors.BLACK
        drawValue(true)

        var color = ColorRGBa.GRAY.shade(0.25)
        if (isHovered) color = mix(color, Colors.SELECTED, 0.7)

        drawer.stroke = color
        drawer.strokeWeight = 1.0
        drawer.lineCap = LineCap.ROUND
        drawer.segment(ls)

        val pLow = ls.position(tLow)
        val pHigh = ls.position(tHigh)
        drawer.stroke = Colors.SELECTED
        drawer.strokeWeight = 2.0
        drawer.lineSegment(pLow, pHigh)

        drawer.strokeWeight = 1.0
        for ((i, p) in listOf(pLow, pHigh).withIndex()) {
            drawer.stroke = null
            drawer.fill = color
            drawer.circle(p, 6.0)
            if (isDragged && activeHandle == i) {
                drawer.fill = Colors.BACKGROUND
                drawer.stroke = color
                drawer.circle(p, 10.0)
                drawer.fill = color
                drawer.stroke = null
                drawer.circle(p, 6.0)
            }
        }

        drawer.fill = ColorRGBa.BLACK
        drawer.stroke = null
        drawer.fontMap = effectiveStyle.font
        drawer.writer {
            box = Rectangle(railStart.x, railStart.y + 5.0, ls.length, 15.0)
            horizontalAlign = 0.0
            verticalAlign = 1.0
            text("${range.start}")
        }
        drawer.writer {
            box = Rectangle(railStart.x, railStart.y + 5.0, ls.length, 15.0)
            horizontalAlign = 1.0
            verticalAlign = 1.0
            text("${range.endInclusive}")
        }
    }
}

fun <T : Comparable<T>> rangeSlider(
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<ClosedRange<T>>,
    bounds: Rectangle? = null,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range, valueRef, bounds, configure)
    registerElement(s)
    return s
}

fun <T : Comparable<T>> rangeSlider(
    range: ClosedRange<T>,
    valueLeftRef: KMutableProperty0<T>,
    valueRightRef: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range,
        getter = { valueLeftRef.get()..valueRightRef.get() },
        setter = {
            valueLeftRef.set(it.start)
            valueRightRef.set(it.endInclusive)
        },
        bounds,
        configure
    )
    registerElement(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.rangeSlider(
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<ClosedRange<T>>,
    bounds: Rectangle? = null,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range, valueRef, bounds, configure)
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.rangeSlider(
    range: ClosedRange<T>,
    valueLeftRef: KMutableProperty0<T>,
    valueRightRef: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range,
        getter = { valueLeftRef.get()..valueRightRef.get() },
        setter = {
            valueLeftRef.set(it.start)
            valueRightRef.set(it.endInclusive)
        },
        bounds,
        configure
    )
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.rangeSlider(
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<ClosedRange<T>>,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range, valueRef, currentElementBounds, configure)
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.rangeSlider(
    range: ClosedRange<T>,
    valueLeftRef: KMutableProperty0<T>,
    valueRightRef: KMutableProperty0<T>,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range,
        getter = { valueLeftRef.get()..valueRightRef.get() },
        setter = {
            valueLeftRef.set(it.start)
            valueRightRef.set(it.endInclusive)
        },
        currentElementBounds,
        configure
    )
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.rangeSlider(
    range: ClosedRange<T>,
    obj: Any,
    valueRef: KMutableProperty1<Any, ClosedRange<T>>,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range, obj, valueRef, currentElementBounds, configure)
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.rangeSlider(
    range: ClosedRange<T>,
    obj: Any,
    valueLeftRef: KMutableProperty1<Any, T>,
    valueRightRef: KMutableProperty1<Any, T>,
    configure: RangeSlider<T>.() -> Unit = {}
): RangeSlider<T> {
    val s = RangeSlider(range,
        getter = { valueLeftRef.get(obj)..valueRightRef.get(obj) },
        setter = {
            valueLeftRef.set(obj, it.start)
            valueRightRef.set(obj, it.endInclusive)
        },
        currentElementBounds,
        configure
    )
    add(s)
    return s
}