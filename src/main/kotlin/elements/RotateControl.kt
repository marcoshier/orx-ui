package elements

import org.openrndr.draw.Drawer
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import registerElement
import style.Colors
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KMutableProperty0

class RotateControl(
    label: String,
    bounds: Rectangle,
    private val getter: () -> Double,
    private val setter: (Double) -> Unit,
    configure: RotateControl.() -> Unit = {}
): UIElementImpl(label, bounds) {

    constructor(
        label: String,
        bounds: Rectangle,
        valueRef: KMutableProperty0<Double>,
        configure: RotateControl.() -> Unit = {}
    ): this(label, bounds, { valueRef.get() }, { valueRef.set(it) }, configure)


    val track = Circle(
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

            totalAngleDeg += delta
            lastAngleDeg = angle

            val newAngle = startAngle + totalAngleDeg

            minAngle = min(newAngle, t)
            maxAngle = max(newAngle, t)

            t = newAngle
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
        if (!visible) return

        drawer.fontMap = style.font

        drawer.stroke = null
        drawer.fill = style.idleColor
        drawer.roundedRectangle(bounds.toRounded(2.0))

        val mainColor = when {
            isHovered && !isDragged -> style.hoverColor
            isDragged -> style.selectColor.shade(0.75)
            else -> style.hoverColor.mix(style.idleColor, 0.25)
        }

        drawer.stroke = mainColor
        drawer.fill = null
        drawer.circle(track)

        drawer.stroke = null
        drawer.fill = mainColor
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
            text("%.1f°".format(value))
        }
    }
}


fun rotateControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Double>,
    configure: RotateControl.() -> Unit = {},
): RotateControl {
    val b = RotateControl(label, bounds, valueRef, configure)
    registerElement(b)
    return b
}

fun WidgetBuilder.rotateControl(
    label: String = "",
    bounds: Rectangle,
    valueRef: KMutableProperty0<Double>,
    configure: RotateControl.() -> Unit = {},
): RotateControl {
    val b = RotateControl(label, bounds, valueRef, configure)
    add(b)
    return b
}
