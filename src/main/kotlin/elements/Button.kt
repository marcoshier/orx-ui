package elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import registerElement
import style.Colors
import style.Fonts
import ui.UIElementImpl
import widgets.WidgetImpl
import kotlin.reflect.KMutableProperty0

class Button(
    label: String,
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
) : UIElementImpl(label, bounds) {

    var action: () -> Unit = {}

    var iconOn: Button.() -> Unit = {}
    var iconOff: Button.() -> Unit = iconOn

    var idleColor = Colors.CLICKABLE
    var hoverColor = Colors.HOVERED
    var focusColor = Colors.FOCUSED
    var selectColor = focusColor

    var border: Boolean = false
    var rounded: Boolean = true
    var borderRadius: Double = 2.0

    var textColor = Colors.BLACK
    var font = Fonts.DEFAULT
    var textAlign = Vector2(0.5)

    init {
        configure()
        buttonDown.listen { it.cancelPropagation() }
        clicked.listen { action() }
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)

        val mainFill = when {
            isHovered -> hoverColor
            isFocused -> focusColor
            isSelected -> selectColor
            else -> idleColor
        }

        drawer.strokeWeight = 0.01
        drawer.stroke = if (border) Colors.BLACK else null
        drawer.fill  = mainFill

        if (rounded) {
            drawer.roundedRectangle(bounds.toRounded(borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        drawer.strokeWeight = 1.0


        if (label.isEmpty()) {
            if (isFocused) { iconOn() } else { iconOff() }
        } else {
            drawer.fill = if (isFocused) ColorRGBa.WHITE - textColor else textColor
            drawer.fontMap = font

            val textBounds = drawer.writer {
                box = bounds.copy(width = 9999.0)
                horizontalAlign = textAlign.x
                verticalAlign = textAlign.y
                text(label, false)
                glyphOutput.rectangles.map { it.second }.bounds
            }

            if (textBounds.width > bounds.width) {
                drawer.isolated {
                    drawer.drawStyle.clip = this@Button.bounds
                    drawer.writer {
                        box = this@Button.bounds.copy(
                            corner = Vector2(this@Button.bounds.x + 5.0, this@Button.bounds.y),
                            width = 9999.0
                        )
                        horizontalAlign = 0.0
                        verticalAlign = textAlign.y
                        text(label)
                    }
                    drawer.drawStyle.clip = null
                }

            } else {
                drawer.writer {
                    box = bounds
                    horizontalAlign = textAlign.x
                    verticalAlign = textAlign.y
                    text(label)
                }
            }
        }

    }
}

fun button(
    label: String,
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(label, bounds, configure)
    registerElement(b)
    return b
}

fun button(
    label: String = "",
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(label, bounds) {
        action = { state.set(!state.get()) }
        beforeDraw = { isFocused = state.get() }
        configure()
    }
    registerElement(b)
    return b
}

fun WidgetImpl.button(
    label: String,
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(label, bounds, configure)
    add(b)
    return b
}

fun WidgetImpl.button(
    label: String = "",
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(label, bounds) {
        action = { state.set(!state.get()) }
        beforeDraw = { isSelected = state.get() }
        configure()
    }
    add(b)
    return b
}