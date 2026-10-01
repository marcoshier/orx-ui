package elements

import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.listen
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import registerElement
import style.Colors
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class XYControl(
    label: String,
    bounds: Rectangle,
    private val getter: () -> Vector2,
    private val setter: (Vector2) -> Unit,
    var rangeX: ClosedFloatingPointRange<Double>,
    var rangeY: ClosedFloatingPointRange<Double>,
    configure: XYControl.() -> Unit
): UIElementImpl(label, bounds.offsetEdges(10.0)) {

    constructor(
        label: String,
        bounds: Rectangle,
        valueRef: KMutableProperty0<Vector2>,
        rangeX: ClosedFloatingPointRange<Double>,
        rangeY: ClosedFloatingPointRange<Double>,
        configure: XYControl.() -> Unit = {}
    ): this(label, bounds, { valueRef.get() }, { valueRef.set(it) },  rangeX, rangeY, configure)

    constructor(
        label: String,
        bounds: Rectangle,
        obj: Any,
        valueRef: KMutableProperty1<Any, Vector2>,
        rangeX: ClosedFloatingPointRange<Double>,
        rangeY: ClosedFloatingPointRange<Double>,
        configure: XYControl.() -> Unit = {}
    ) : this(label, bounds, { valueRef.get(obj) }, { valueRef.set(obj, it) }, rangeX, rangeY, configure)

    private val ibounds = bounds.copy()

    var uv: Vector2
        get() {
            val current = getter()
            return current.map(
                Vector2(rangeX.start, rangeY.start),
                Vector2(rangeX.endInclusive, rangeY.endInclusive),
                Vector2.ZERO, Vector2.ONE, true
            )
        }
        set(value) {
            val ranged = value.map(
                Vector2.ZERO, Vector2.ONE,
                Vector2(rangeX.start, rangeY.start),
                Vector2(rangeX.endInclusive, rangeY.endInclusive),
                true
            )

            setter(ranged)
        }

    private fun setUV(position: Vector2) {
        uv = position.map(
            ibounds.corner, ibounds.corner + ibounds.dimensions,
            Vector2.ZERO, Vector2.ONE, true
        )
    }

    init {
        buttonDown.listen {
            it.cancelPropagation()
        }

        listOf(dragged, buttonUp).listen {
            setUV(it.position)
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)

        drawer.fontMap = style.font

        drawer.stroke = style.stroke
        drawer.fill = style.idleColor
        drawer.roundedRectangle(ibounds.toRounded(2.0))

        val textBoxOffset = 4.0

        fun drawRangeText(value: Double, xAlign: Double, yAlign: Double, rotate: Boolean = false) {
            drawer.isolated {
                if (rotate) {
                    translate(ibounds.center)
                    rotate(-90.0)
                    translate(-ibounds.center)
                }
                drawer.fill = style.textColor.opacify(0.75)
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
                box = Rectangle.fromCenter(ibounds.position(uv) + Vector2(0.0, fh * 2.5), 300.0, fh * 1.5) // TODO
                horizontalAlign = 0.5
                verticalAlign = 0.5
                val value = getter()
                val x = "%.1f".format(value.x)
                val y = "%.1f".format(value.y)
                text("$x,$y", visible)
                glyphOutput.rectangles.map { it.second }.bounds
            }
        }

        drawRangeText(rangeX.start, 0.03, 1.0)
        drawRangeText(rangeX.endInclusive, 0.97, 1.0)
        drawRangeText(rangeY.start, 0.03, 0.0, true)
        drawRangeText(rangeY.endInclusive, 1.0, 0.0, true)

        drawer.stroke = null
        drawer.fill = style.hoverColor
        val textBounds = drawValueText(false).offsetEdges(2.0)
        drawer.rectangle(textBounds)
        drawer.fill = style.textColor
        drawValueText(true)

        val bpos = ibounds.position(uv)
        drawer.stroke = style.hoverColor
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
        drawer.fill = style.selectColor
        drawer.circle(ibounds.position(uv), 5.0)
    }
}

fun xyControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Vector2>,
    rangeX: ClosedFloatingPointRange<Double>,
    rangeY: ClosedFloatingPointRange<Double>,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl(label, bounds, valueRef, rangeX, rangeY, configure)
    registerElement(b)
    return b
}

fun xyControl(
    label: String = "",
    bounds: Rectangle,
    obj: Any,
    valueRef: KMutableProperty1<Any, Vector2>,
    rangeX: ClosedFloatingPointRange<Double>,
    rangeY: ClosedFloatingPointRange<Double>,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl(label, bounds, obj, valueRef, rangeX, rangeY, configure)
    registerElement(b)
    return b
}

fun WidgetBuilder.xyControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Vector2>,
    rangeX: ClosedFloatingPointRange<Double>,
    rangeY: ClosedFloatingPointRange<Double>,
    configure: XYControl.() -> Unit = {},
): XYControl {
    val b = XYControl(label, bounds, valueRef, rangeX, rangeY, configure)
    add(b)
    return b
}

