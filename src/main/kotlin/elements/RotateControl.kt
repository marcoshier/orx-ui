package elements

import org.openrndr.draw.Drawer
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import style.Colors
import style.Style
import style.styleProperty
import ui.UIElement
import ui.registerElement
import widget.WidgetBuilder
import kotlin.math.atan2
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class RotateControl(
    private val getter: () -> Double,
    private val setter: (Double) -> Unit,
    bounds: Rectangle? = null,
    configure: RotateControl.() -> Unit = {}
): UIElement(bounds ?: Rectangle(10.0, 10.0, 200.0)) {

    constructor(
        valueRef: KMutableProperty0<Double>,
        bounds: Rectangle? = null,
        configure: RotateControl.() -> Unit = {}
    ): this({ valueRef.get() }, { valueRef.set(it) }, bounds, configure)

    @Suppress("UNCHECKED_CAST")
    constructor(
        obj: Any,
        valueRef: KMutableProperty1<Any, Double>,
        bounds: Rectangle? = null,
        configure: RotateControl.() -> Unit = {}
    ): this({ valueRef.get(obj) }, { valueRef.set(obj, it) }, bounds, configure)

    val track
        get() = Circle(
            bounds.center,
            bounds.width * 0.4
        )

    var t: Double
        get() = getter()
        set(value) {
            setter(value)
        }

    init {
        var minAngle = Double.MAX_VALUE
        var maxAngle = Double.MIN_VALUE
        var startAngle = 0.0
        var lastAngleDeg = 0.0
        var totalAngleDeg = 0.0

        fun getAngle(pos: Vector2): Double {
            val raw = Math.toDegrees(atan2(
                pos.y - track.center.y,
                pos.x - track.center.x
            ))
            return raw - 90.0
        }

        fun setAngle(pos: Vector2) {
            val angle = getAngle(pos)
            var delta = angle - lastAngleDeg
            if (delta > 180.0) delta -= 360.0
            if (delta < -180.0) delta += 360.0

            val friction = precision.coerceAtLeast(1).toDouble()
            totalAngleDeg += delta / friction

            lastAngleDeg = angle
            t = startAngle + totalAngleDeg
        }

        buttonDown.listen {
            it.cancelPropagation()
            startAngle = t
            lastAngleDeg = getAngle(it.position)
            totalAngleDeg = 0.0
        }

        dragged.listen {
            setAngle(it.position)
        }

        buttonUp.listen {
            setAngle(it.position)
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible()) return

        drawer.fontMap = effectiveStyle.fontMap

        drawer.stroke = null
        drawer.fill = effectiveStyle.backgroundColor
        drawer.roundedRectangle(bounds.toRounded(2.0))

        drawer.stroke = effectiveStyle.rotateSliderStroke
        drawer.fill = effectiveStyle.rotateSliderFill
        drawer.circle(track)

        drawer.stroke = null
        drawer.fill = effectiveStyle.rotateSliderStroke
        drawer.circle(
            track.contour.position(((t + 90.0) / 360.0).mod(1.0)), 5.0
        )

        drawer.fill = Colors.BLACK
        drawer.writer {
            val fh = drawer.fontMap!!.height
            box = Rectangle.fromCenter(bounds.center, 300.0, fh * 1.5)
            horizontalAlign = 0.5
            verticalAlign = 0.5
            val value = getter()
            text("%.${precision}f°".format(value))
        }
    }
}


var Style.rotateSliderStroke by styleProperty(Colors.CLICKABLE.shade(0.5))
var Style.rotateSliderFill by styleProperty(null)

fun rotateControl(
    valueRef: KMutableProperty0<Double>,
    bounds: Rectangle? = null,
    configure: RotateControl.() -> Unit = {},
): RotateControl {
    val b = RotateControl(valueRef, bounds, configure)
    registerElement(b)
    return b
}

fun WidgetBuilder.rotateControl(
    valueRef: KMutableProperty0<Double>,
    bounds: Rectangle? = null,
    configure: RotateControl.() -> Unit = {},
): RotateControl {
    val b = RotateControl(valueRef, bounds, configure)
    add(b)
    return b
}

fun WidgetBuilder.rotateControl(
    obj: Any,
    valueRef: KMutableProperty1<Any, Double>,
    configure: RotateControl.() -> Unit = {},
): RotateControl {
    val b = RotateControl(obj, valueRef, currentElementBounds, configure)
    add(b)
    return b
}