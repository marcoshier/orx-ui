package elements

import lib.coerceIn
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.LineSegment
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
open class Slider<T: Comparable<T>>(
    var range: ClosedRange<T>,
    private val getter: () -> T,
    private val setter: (T) -> Unit,
    bounds: Rectangle? = null,
    configure: Slider<T>.() -> Unit = {}
): UIElement(bounds ?: Rectangle(10.0, 10.0, 60.0, 30.0)) {

    constructor(
        range: ClosedRange<T>,
        valueRef: KMutableProperty0<T>,
        bounds: Rectangle? = null,
        configure: Slider<T>.() -> Unit = {}
    ): this(range, { valueRef.get() }, { valueRef.set(it) }, bounds, configure)

    constructor(
        range: ClosedRange<T>,
        obj: Any,
        valueRef: KMutableProperty1<Any, T>,
        bounds: Rectangle? = null,
        configure: Slider<T>.() -> Unit = {}
    ) : this(range, { valueRef.get(obj) }, { valueRef.set(obj, it) }, bounds, configure)

    val railStart
        get() = Vector2(bounds.x + 4.0, bounds.y + bounds.height / 2.0)

    val railEnd
        get() = Vector2(bounds.x + bounds.width - 4.0, bounds.y + bounds.height / 2.0)

    var t: Double
        get() {
            return when (val current = getter()) {
                is Int -> {
                    val r = range as IntRange
                    current.toDouble().map(r.first.toDouble(), r.last.toDouble(), 0.0, 1.0, true)
                }
                is Double -> {
                    val r = range as ClosedFloatingPointRange<Double>
                    current.map(r.start, r.endInclusive, 0.0, 1.0, true)
                }
                else -> 0.5
            }
        }
        set(value) {
            val ranged: T = when(val current = getter()) {
                is Int -> {
                    val r = range as IntRange
                    value.map(0.0, 1.0, r.first.toDouble(), r.last.toDouble(), true).roundToInt() as T
                }
                is Double -> {
                    val r = range as ClosedFloatingPointRange<Double>
                    value.map(0.0, 1.0, r.start, r.endInclusive, true) as T
                }
                else -> error("Unsupported type: ${current::class}")
            }

            setter(ranged)
        }

    private var dragStartValue: T? = null

    fun updateT(x: Double) {
       t = x.map(railStart.x, railEnd.x, 0.0, 1.0, true)
    }

    fun updateT(x: Double, friction: Double) {
        t = (t + (x / friction)).coerceIn(0.0, 1.0)
    }

    init {
        var lastX = 0.0
        var dragging = false
        var dragT = 0.0

        buttonDown.listen {
            it.cancelPropagation()
            dragStartValue = getter()
            lastX = it.position.x
            dragT = t
        }

        dragged.listen {
            dragging = true
            val length = railEnd.x - railStart.x
            val dx = it.position.x - lastX
            lastX = it.position.x

            val friction = precision.coerceAtLeast(1) * 1.0
            dragT = (dragT + dx / length / friction).coerceIn(0.0, 1.0)
            t = dragT
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible()) return

        drawer.stroke = effectiveStyle.strokeColor
        drawer.fill = effectiveStyle.backgroundColor

        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(bounds.toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        if (label.isNotEmpty()) {
            drawer.fontMap = effectiveStyle.fontMap
            drawer.fill = effectiveStyle.textColor
            drawer.stroke = null
            drawer.writer {
                box = bounds.offsetEdges(-4.0)
                horizontalAlign = 0.5
                verticalAlign = 1.0
                text(label.uppercase())
            }
        }

        drawer.fill = effectiveStyle.sliderInactiveRailFill
        drawer.stroke = effectiveStyle.sliderRailStroke
        val b = bounds.offsetEdges(-4.0).copy(height = (bounds.height - effectiveStyle.fontMap.ascenderLength).coerceAtLeast(2.0) - 8.0)
        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(b.toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(b)
        }

        val rail = Segment2D(railStart, railEnd)
        val p = rail.position(t)
        drawer.isolated {
            drawer.drawStyle.smooth = false

            val ls = LineSegment(p.x, b.y - 1.0, p.x, b.y + b.height + 0.5)
            val rr = Rectangle(b.corner, p.x - b.x, b.height)

            drawer.stroke = null
            drawer.fill = effectiveStyle.sliderActiveRailFill
            if (effectiveStyle.borderRadius > 0.0) {
                drawer.roundedRectangle(rr.toRounded(effectiveStyle.borderRadius))
            } else {
                drawer.rectangle(rr)
            }

            drawer.strokeWeight = 2.0
            drawer.stroke = effectiveStyle.sliderHandleStroke
            drawer.lineSegment(ls)
        }

        fun displayValue(v: T) = when(v) {
            is Double -> "%.${precision}f".format(v)
            else -> "$v"
        }

        fun drawValue(visible: Boolean, rect: Rectangle? = null): Rectangle {
            drawer.fontMap = effectiveStyle.fontMap
            return drawer.writer {
                box = rect ?: Rectangle(0.0, 0.0, 9999.0, effectiveStyle.fontMap.ascenderLength)
                horizontalAlign = 0.0
                verticalAlign = 0.0
                text(displayValue(getter()), visible)
                glyphOutput.rectangles.map { it.second }.bounds
            }
        }

        val r = drawValue(false)
        drawer.stroke = null
        drawer.fill = effectiveStyle.sliderActiveTextColor
        val target = Rectangle(
            p.x + 2.0,
            b.y + (b.height - r.height) / 2.0 + 1.5,
            r.width,
            r.height
        ).coerceIn(b.offsetEdges(-1.0))

        drawValue(true, target)

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


fun <T : Comparable<T>> slider(
    range: ClosedFloatingPointRange<T>,
    valueRef: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    configure: Slider<T>.() -> Unit = {}
): Slider<T> {
    val s = Slider(range, valueRef, bounds, configure)
    registerElement(s)
    return s
}


fun <T : Comparable<T>> slider(
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    configure: Slider<T>.() -> Unit = {}
): Slider<T> {
    val s = Slider(range, valueRef, bounds, configure)
    registerElement(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.slider(
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<T>,
    bounds: Rectangle? = null,
    configure: Slider<T>.() -> Unit = {}
): Slider<T> {
    val s = Slider(range, valueRef, bounds, configure)
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.slider(
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<T>,
    configure: Slider<T>.() -> Unit = {},
): Slider<T> {
    val b = Slider(range, valueRef, currentElementBounds, configure)
    add(b)
    return b
}

fun <T : Comparable<T>> WidgetBuilder.slider(
    range: ClosedRange<T>,
    obj: Any,
    valueRef: KMutableProperty1<Any, T>,
    configure: Slider<T>.() -> Unit = {},
): Slider<T> {
    val b = Slider(range, obj, valueRef, currentElementBounds, configure)
    add(b)
    return b
}
