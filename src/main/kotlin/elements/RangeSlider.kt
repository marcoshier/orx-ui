package elements

import lib.coerceIn
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Segment2D
import org.openrndr.shape.bounds
import ui.registerElement
import ui.UIElement
import widget.WidgetBuilder
import kotlin.math.roundToInt
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
        get() = Vector2(bounds.x + 4.0, bounds.y + bounds.height / 2.0)

    val railEnd: Vector2
        get() = Vector2(bounds.x + bounds.width - 4.0, bounds.y + bounds.height / 2.0)


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
            t.map(0.0, 1.0, r.first.toDouble(), r.last.toDouble(), true).roundToInt() as T
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

        val s = effectiveStyle

        fun box(r: Rectangle) {
            if (s.borderRadius > 0.0) drawer.roundedRectangle(r.toRounded(s.borderRadius))
            else drawer.rectangle(r)
        }

        drawer.stroke = s.strokeColor
        drawer.fill = s.backgroundColor
        box(bounds)

        drawer.fontMap = s.fontMap
        if (label.isNotEmpty()) {
            drawer.fill = s.textColor
            drawer.stroke = null
            drawer.writer {
                box = bounds.offsetEdges(-4.0)
                horizontalAlign = 0.5
                verticalAlign = 1.0
                text(label.uppercase())
            }
        }

        val b = bounds.offsetEdges(-4.0)
            .copy(height = (bounds.height - s.fontMap.ascenderLength).coerceAtLeast(2.0) - 8.0)
        drawer.fill = s.sliderInactiveRailFill
        drawer.stroke = s.sliderRailStroke
        box(b)

        val rail = Segment2D(railStart, railEnd)
        val pLow = rail.position(tLow)
        val pHigh = rail.position(tHigh)

        drawer.isolated {
            drawer.drawStyle.smooth = false

            drawer.stroke = null
            drawer.fill = s.sliderActiveRailFill
            box(Rectangle(pLow.x, b.y - 1.0, pHigh.x - pLow.x, b.height + 1.5))

            drawer.strokeWeight = 2.0
            drawer.stroke = s.sliderHandleStroke
            for (p in listOf(pLow, pHigh)) {
                drawer.lineSegment(p.x, b.y - 1.0, p.x, b.y + b.height + 0.5)
            }
        }

        fun displayValue(v: T) = when (v) {
            is Double -> "%.${precision}f".format(v)
            else -> "$v"
        }

        fun drawValue(str: String, visible: Boolean, rect: Rectangle? = null): Rectangle {
            drawer.fontMap = s.fontMap
            return drawer.writer {
                box = rect ?: Rectangle(0.0, 0.0, 9999.0, s.fontMap.ascenderLength)
                horizontalAlign = 0.0
                verticalAlign = 0.0
                text(str, visible)
                glyphOutput.rectangles.map { it.second }.bounds
            }
        }

        val v = getter()
        val area = b.offsetEdges(-1.0)

        drawer.stroke = null
        drawer.fill = s.sliderActiveTextColor

        val lowStr = displayValue(v.start)
        val lr = drawValue(lowStr, false)
        val lowTarget = Rectangle(
            pLow.x - 2.0 - lr.width,
            b.y + (b.height - lr.height) / 2.0 + 1.5,
            lr.width,
            lr.height
        ).coerceIn(area)
        drawValue(lowStr, true, lowTarget)

        val highStr = displayValue(v.endInclusive)
        val hr = drawValue(highStr, false)
        val highTarget = Rectangle(
            pHigh.x + 2.0,
            b.y + (b.height - hr.height) / 2.0 + 1.5,
            hr.width,
            hr.height
        ).coerceIn(area)
        drawValue(highStr, true, highTarget)

        drawer.fill = s.textColor
        drawer.writer {
            box = bounds.offsetEdges(-4.0)
            horizontalAlign = 0.0
            verticalAlign = 1.0
            text(displayValue(range.start))
        }
        drawer.writer {
            box = bounds.offsetEdges(-4.0)
            horizontalAlign = 1.0
            verticalAlign = 1.0
            text(displayValue(range.endInclusive))
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