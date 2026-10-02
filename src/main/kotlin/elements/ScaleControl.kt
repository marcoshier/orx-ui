package elements

import lib.coerceIn
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.listen
import org.openrndr.extra.math.linearrange.LinearRange1D
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import registerElement
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.math.abs
import kotlin.reflect.KMutableProperty0

class ScaleControl (
    label: String,
    bounds: Rectangle,
    private val getter: () -> Vector2,
    private val setter: (Vector2) -> Unit,
    val range: LinearRange1D<Vector2>,
    configure: ScaleControl.() -> Unit
): UIElementImpl(label, bounds) {

    private val ibounds = bounds.offsetEdges(-15.0)
    var invert = false
    var uniform = false

    constructor(
        label: String,
        bounds: Rectangle,
        valueRef: KMutableProperty0<Vector2>,
        range: LinearRange1D<Vector2>,
        configure: ScaleControl.() -> Unit = {}
    ): this(label, bounds, { valueRef.get() }, { valueRef.set(it) }, range, configure)

    constructor(
        label: String,
        bounds: Rectangle,
        valueRef: KMutableProperty0<Double>,
        range: ClosedFloatingPointRange<Double>,
        configure: ScaleControl.() -> Unit = {}
    ): this(
        label, bounds, { Vector2(valueRef.get()) }, { valueRef.set(it.x) },
        Vector2(range.start)..Vector2(range.endInclusive), configure
    ) {
        uniform = true
    }

    var mappedScale: Vector2
        get() {
            val current = getter()
            if (!current.x.isFinite() || !current.y.isFinite())
                return Vector2.ZERO

            val afterLeft = if (invert) Vector2.ONE else Vector2.ZERO
            val afterRight = if (invert) Vector2.ZERO else Vector2.ONE
            return current.map(range.start, range.end, afterLeft, afterRight, true)
        }
        set(value) {
            if (!value.x.isFinite() || !value.y.isFinite())
                return

            val beforeLeft = if (invert) Vector2.ONE else Vector2.ZERO
            val beforeRight = if (invert) Vector2.ZERO else Vector2.ONE
            val ranged = value.map(beforeLeft, beforeRight, range.start, range.end, true)

            if (ranged.x.isFinite() || ranged.y.isFinite()) {
                setter(ranged)
            }
        }

    init {
        buttonDown.listen {
            it.cancelPropagation()
        }

        listOf(dragged, buttonUp).listen {
            val half = Vector2(ibounds.width / 2.0, ibounds.height / 2.0)
            val diff = it.position - ibounds.center
            var n = Vector2(
                (abs(diff.x) / half.x).coerceIn(0.0, 1.0),
                (abs(diff.y) / half.y).coerceIn(0.0, 1.0)
            )
            if (uniform) {
                val u = maxOf(n.x, n.y)
                n = Vector2(u, u)
            }
            mappedScale = n
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible) return

        drawer.fontMap = style.font

        drawer.stroke = style.stroke
        drawer.fill = style.idleColor
        drawer.roundedRectangle(bounds.toRounded(2.0))

        drawer.strokeWeight = 15.0
        drawer.stroke = style.hoverColor.mix(style.idleColor, 0.55).opacify(0.5)
        drawer.fill = null
        drawer.roundedRectangle(bounds.offsetEdges(-7.5).toRounded(2.0))
        drawer.strokeWeight = 1.0


        val offsetBounds = bounds.offsetEdges(-15.0)
        val scaleRect = ibounds.scaledBy(mappedScale.x, mappedScale.y, 0.5, 0.5)

        drawer.stroke = style.hoverColor.mix(style.idleColor, 0.4)
        drawer.strokeWeight = 0.1
        drawer.lineSegment(scaleRect.x, offsetBounds.y, scaleRect.x, offsetBounds.y + offsetBounds.height)
        drawer.lineSegment(scaleRect.x + scaleRect.width, offsetBounds.y, scaleRect.x + scaleRect.width, offsetBounds.y + offsetBounds.height)
        drawer.lineSegment(offsetBounds.x, scaleRect.y, offsetBounds.x + offsetBounds.width, scaleRect.y)
        drawer.lineSegment(offsetBounds.x, scaleRect.y + scaleRect.height, offsetBounds.x + offsetBounds.width, scaleRect.y + scaleRect.height)

        if (scaleRect.area > 0.0) {
            drawer.stroke = null
            drawer.fill = if (isDragged) style.selectColor else style.hoverColor
            drawer.roundedRectangle(scaleRect.toRounded(2.0))
        }

        val textBoxOffset = 4.0

        fun drawRangeText(value: Double, xAlign: Double, yAlign: Double, rotate: Boolean = false) {
            drawer.isolated {
                if (rotate) {
                    translate(this@ScaleControl.bounds.center)
                    rotate(-90.0)
                    translate(-this@ScaleControl.bounds.center)
                }
                drawer.fill = style.textColor
                drawer.writer {
                    box = this@ScaleControl.bounds.offsetEdges(-textBoxOffset)
                    horizontalAlign = xAlign
                    verticalAlign = yAlign
                    val value = "%.1f".format(value)
                    text(value)
                }
            }
        }

        fun drawValueText(visible: Boolean): Rectangle {
            return drawer.writer {
                box = if (scaleRect.area > 1.0) {
                    Rectangle.fromCenter(scaleRect.position(0.5, 1.05), 200.0, 15.5).coerceIn(bounds)
                } else bounds
                horizontalAlign = 0.5
                verticalAlign = 0.5
                val value = getter()
                val x = "%.1f".format(value.x)
                val y = "%.1f".format(value.y)
                val text = if (uniform) x else "$x,$y"
                text(text, visible)
                glyphOutput.rectangles.map { it.second }.bounds
            }
        }

        drawRangeText(range.start.x, 0.03, 1.0)
        drawRangeText(range.end.x, 0.97, 1.0)
        drawRangeText(range.start.y, 0.03, 0.0, true)
        drawRangeText(range.end.y, 0.97, 0.0, true)


        val textRect = drawValueText(false)
        drawer.stroke = null
        drawer.fill = style.hoverColor
        drawer.rectangle(textRect)
        drawer.fill = style.textColor
        drawValueText(true)
    }
}


fun scaleControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Vector2>,
    range: LinearRange1D<Vector2>,
    configure: ScaleControl.() -> Unit = {},
): ScaleControl {
    val b = ScaleControl(label, bounds, valueRef, range, configure)
    registerElement(b)
    return b
}

fun scaleControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Double>,
    range: ClosedFloatingPointRange<Double>,
    configure: ScaleControl.() -> Unit = {},
): ScaleControl {
    val b = ScaleControl(label, bounds, valueRef, range, configure)
    registerElement(b)
    return b
}

fun WidgetBuilder.scaleControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Vector2>,
    range: LinearRange1D<Vector2>,
    configure: ScaleControl.() -> Unit = {},
): ScaleControl {
    val b = ScaleControl(label, bounds, valueRef, range, configure)
    add(b)
    return b
}

fun WidgetBuilder.scaleControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Double>,
    range: ClosedFloatingPointRange<Double>,
    configure: ScaleControl.() -> Unit = {},
): ScaleControl {
    val b = ScaleControl(label, bounds, valueRef, range, configure)
    add(b)
    return b
}