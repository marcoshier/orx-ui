package elements

import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.listen
import org.openrndr.extra.math.linearrange.LinearRange1D
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import style.Colors
import style.Style
import style.styleProperty
import ui.UIElement
import ui.registerElement
import widget.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class XYControl(
    private val getter: () -> Vector2,
    private val setter: (Vector2) -> Unit,
    var range: LinearRange1D<Vector2>,
    bounds: Rectangle? = null,
    configure: XYControl.() -> Unit = {}
): UIElement(bounds ?: Rectangle(10.0, 10.0, 200.0)) {

    constructor(
        valueRef: KMutableProperty0<Vector2>,
        range: LinearRange1D<Vector2>,
        bounds: Rectangle? = null,
        configure: XYControl.() -> Unit = {}
    ): this({ valueRef.get() }, { valueRef.set(it) }, range, bounds, configure)

    constructor(
        obj: Any,
        valueRef: KMutableProperty1<Any, Vector2>,
        range: LinearRange1D<Vector2>,
        bounds: Rectangle? = null,
        configure: XYControl.() -> Unit = {}
    ) : this({ valueRef.get(obj) }, { valueRef.set(obj, it) }, range, bounds, configure)

    private val ibounds
        get() = bounds.copy()

    var uv: Vector2
        get() {
            val current = getter()
            return current.map(range.start, range.end, Vector2.ZERO, Vector2.ONE, true)
        }
        set(value) {
            val ranged = value.map(Vector2.ZERO, Vector2.ONE, range.start, range.end, true)
            setter(ranged)
        }

    private fun setUV(position: Vector2) {
        uv = position.map(
            ibounds.corner, ibounds.corner + ibounds.dimensions,
            Vector2.ZERO, Vector2.ONE, true
        )
    }

    init {
        var lastPos = Vector2.ZERO

        buttonDown.listen {
            it.cancelPropagation()
            lastPos = it.position
        }

        listOf(dragged, buttonUp).listen {
            val delta = it.position - lastPos
            lastPos = it.position

            val friction = precision.coerceAtLeast(1).toDouble()
            val duv = Vector2(delta.x / ibounds.width, delta.y / ibounds.height) / friction
            uv = (uv + duv).clamp(Vector2.ZERO, Vector2.ONE)
        }

        configure()
    }
    override fun draw(drawer: Drawer) {
        super.draw(drawer)

        drawer.fontMap = effectiveStyle.fontMap
        drawer.stroke = effectiveStyle.strokeColor
        drawer.fill = effectiveStyle.backgroundColor
        drawer.roundedRectangle(ibounds.toRounded(2.0))

        val textBoxOffset = 4.0

        fun drawRangeText(value: Double, xAlign: Double, yAlign: Double, rotate: Boolean = false) {
            drawer.isolated {
                if (rotate) {
                    translate(ibounds.center)
                    rotate(-90.0)
                    translate(-ibounds.center)
                }
                drawer.fill = effectiveStyle.textColor?.opacify(0.75)
                drawer.writer {
                    box = ibounds.offsetEdges(-textBoxOffset)
                    horizontalAlign = xAlign
                    verticalAlign = yAlign
                    val value = "%.1f".format(value)
                    text(value)
                }
            }
        }

        fun drawValueText(visible: Boolean): Rectangle {
            return drawer.writer {
                val fh = drawer.fontMap!!.height
                box = Rectangle.fromCenter(ibounds.position(uv) + Vector2(0.0, fh * 2.5), 300.0, fh * 1.5)
                horizontalAlign = 0.5
                verticalAlign = 0.5
                val value = getter()
                val x = "%.${precision}f".format(value.x)
                val y = "%.${precision}f".format(value.y)
                text("$x,$y", visible)
                glyphOutput.rectangles.map { it.second }.bounds
            }
        }

        drawRangeText(range.start.x, 0.03, 1.0)
        drawRangeText(range.end.x, 0.97, 1.0)
        drawRangeText(range.start.y, 0.03, 0.0, true)
        drawRangeText(range.end.y, 1.0, 0.0, true)

        drawer.fill = effectiveStyle.textColor
        drawValueText(true)

        val bpos = ibounds.position(uv)
        drawer.stroke = effectiveStyle.xyControlLineStroke
        drawer.lineSegment(
            Vector2(bpos.x, ibounds.y),
            Vector2(bpos.x, ibounds.y + ibounds.height)
        )
        drawer.lineSegment(
            Vector2(ibounds.x, bpos.y),
            Vector2(ibounds.x + ibounds.width, bpos.y)
        )

        drawer.stroke = Colors.BLACK
        drawer.fill = null
        drawer.circle(ibounds.position(uv), 10.0)

        drawer.stroke = null
        drawer.fill = effectiveStyle.xyControlHandleFill
        drawer.circle(ibounds.position(uv), 5.0)
    }
}


var Style.xyControlLineStroke by styleProperty(Colors.CLICKABLE.shade(0.65))
var Style.xyControlHandleFill by styleProperty(Colors.HOVERED.shade(0.65))


fun xyControl(
    valueRef: KMutableProperty0<Vector2>,
    range: LinearRange1D<Vector2>,
    bounds: Rectangle? = null,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl({ valueRef.get() }, { valueRef.set(it) }, range, bounds, configure)
    registerElement(b)
    return b
}

fun xyControl(
    obj: Any,
    valueRef: KMutableProperty1<Any, Vector2>,
    range: LinearRange1D<Vector2>,
    bounds: Rectangle? = null,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl({ valueRef.get(obj) }, { valueRef.set(obj, it) }, range, bounds, configure)
    registerElement(b)
    return b
}

fun WidgetBuilder.xyControl(
    valueRef: KMutableProperty0<Vector2>,
    range: LinearRange1D<Vector2>,
    bounds: Rectangle? = null,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl({ valueRef.get() }, { valueRef.set(it) }, range, bounds, configure)
    add(b)
    return b
}

fun WidgetBuilder.xyControl(
    obj: Any,
    valueRef: KMutableProperty1<Any, Vector2>,
    range: LinearRange1D<Vector2>,
    bounds: Rectangle? = null,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl({ valueRef.get(obj) }, { valueRef.set(obj, it) }, range, bounds, configure)
    add(b)
    return b
}

fun WidgetBuilder.xyControl(
    obj: Any,
    valueRef: KMutableProperty1<Any, Vector2>,
    range: LinearRange1D<Vector2>,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl({ valueRef.get(obj) }, { valueRef.set(obj, it) }, range, currentElementBounds, configure)
    add(b)
    return b
}