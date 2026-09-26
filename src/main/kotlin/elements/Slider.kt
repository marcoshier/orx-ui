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
import registerElement
import style.Colors
import style.Fonts
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

@Suppress("UNCHECKED_CAST")
open class Slider<T: Comparable<T>>(
    override var label: String = "",
    override var bounds: Rectangle,
    var range: ClosedRange<T>,
    private val getter: () -> T,
    private val setter: (T) -> Unit,
): UIElementImpl(label, bounds) {

    constructor(
        label: String,
        bounds: Rectangle,
        range: ClosedRange<T>,
        valueRef: KMutableProperty0<T>
    ): this(label, bounds, range, { valueRef.get() }, { valueRef.set(it) })

    constructor(
        label: String,
        bounds: Rectangle,
        range: ClosedRange<T>,
        obj: Any,
        valueRef: KMutableProperty1<Any, T>
    ) : this(label, bounds, range, { valueRef.get(obj) }, { valueRef.set(obj, it) })

    val railStart: Vector2
        get() = bounds.position(0.05, 0.5)

    val railEnd: Vector2
        get() = bounds.position(0.95, 0.5)

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
                    (value.map(0.0, 1.0, r.first.toDouble(), r.last.toDouble(), true)).toInt() as T
                }
                is Double -> {
                    val r = range as ClosedFloatingPointRange<Double>
                    value.map(0.0, 1.0, r.start, r.endInclusive, true) as T
                }
                else -> error("Unsupported type: ${current::class}")
            }

            setter(ranged)
        }

    private fun getDoubleValue(): Double {
        val valueDouble = when(val value =  getter()) {
            is Double -> value
            is Int -> value.toDouble()
            else -> error("Unsupported type: ${value::class}")
        }

        return valueDouble
    }

    private var dragStartValue: T? = null

    fun updateT(x: Double) {
       t = x.map(railStart.x, railEnd.x, 0.0, 1.0, true)
    }

    init {
        buttonDown.listen {
            it.cancelPropagation()
            dragStartValue = getter()
        }

        buttonUp.listen {
            updateT(it.position.x)
            dragStartValue = null
        }

        dragged.listen {
            updateT(it.position.x)
        }
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)

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
                val displayValue = when (val v = getter()) {
                    is Double -> "%.1f".format(v)
                    else -> "$v"
                }
                text(displayValue, visible)
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
        if (isHovered) {
            color = mix(color, Colors.SELECTED, 0.7)
        }

        drawer.stroke = color
        drawer.strokeWeight = 1.0
        drawer.lineCap = LineCap.ROUND
        drawer.segment(ls)

        drawer.stroke = null
        drawer.fill = color
        drawer.circle(ls.position(t), 6.0)

        if (isDragged) {
            drawer.fill = Colors.BACKGROUND
            drawer.stroke = color
            drawer.circle(ls.position(t), 10.0)
            drawer.fill = color
            drawer.stroke = null
            drawer.circle(ls.position(t), 6.0)
        }

        drawer.fill = ColorRGBa.BLACK
        drawer.stroke = null
        drawer.fontMap = Fonts.DEFAULT
        drawer.writer {
            box = Rectangle(railStart.x, railStart.y + 5.0, ls.length, 15.0)
            horizontalAlign = 0.0
            verticalAlign = 1.0
            text("${range.start}")
        }

        drawer.fill = ColorRGBa.BLACK
        drawer.stroke = null
        drawer.fontMap = Fonts.DEFAULT
        drawer.writer {
            box = Rectangle(railStart.x, railStart.y + 5.0, ls.length, 15.0)
            horizontalAlign = 1.0
            verticalAlign = 1.0
            text("${range.endInclusive}")
        }
    }

}


fun <T : Comparable<T>> slider(
    label: String,
    bounds: Rectangle,
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<T>,
): Slider<T> {
    val s = Slider(label, bounds, range, valueRef)
    registerElement(s)
    return s
}

fun <T : Comparable<T>> slider(
    label: String,
    bounds: Rectangle,
    range: ClosedRange<T>,
    obj: Any,
    valueRef: KMutableProperty1<Any, T>,
): Slider<T> {
    val s = Slider(label, bounds, range, obj, valueRef)
    registerElement(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.slider(
    label: String,
    bounds: Rectangle,
    range: ClosedRange<T>,
    valueRef: KMutableProperty0<T>,
): Slider<T> {
    val s = Slider(label, bounds, range, valueRef)
    add(s)
    return s
}

fun <T : Comparable<T>> WidgetBuilder.slider(
    label: String,
    bounds: Rectangle,
    range: ClosedRange<T>,
    obj: Any,
    valueRef: KMutableProperty1<Any, T>,
): Slider<T> {
    val s = Slider(label, bounds, range, obj, valueRef)
    add(s)
    return s
}