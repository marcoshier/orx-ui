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
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0

open class Button(
    label: String,
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
) : UIElementImpl(label, bounds) {

    var action: () -> Unit = {}

    var iconOn: Button.() -> Unit = {}
    var iconOff: Button.() -> Unit = iconOn

    var textAlign = Vector2(0.5)

    init {
        buttonDown.listen {
            it.cancelPropagation()
            isSelected = true
        }
        clicked.listen { action() }
        buttonUp.listen {
            isSelected = false
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)

        val mainFill = when {
            isHovered && !isSelected -> style.hoverColor
            isHovered && isSelected -> style.selectColor.shade(0.75)
            isFocused -> style.focusColor
            isSelected -> style.selectColor
            else -> style.idleColor
        }

        drawer.strokeWeight = 0.01
        drawer.stroke = style.stroke
        drawer.fill  = mainFill

        if (style.rounded) {
            drawer.roundedRectangle(bounds.toRounded(style.borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        drawer.strokeWeight = 1.0


        if (label.isEmpty()) {
            if (isFocused) { iconOn() } else { iconOff() }
        } else {
            drawer.fill = if (isFocused) ColorRGBa.WHITE - style.textColor else style.textColor
            drawer.fontMap = style.font

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

fun WidgetBuilder.button(
    label: String,
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(label, bounds, configure)
    add(b)
    return b
}

fun WidgetBuilder.button(
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

fun WidgetBuilder.button(
    label: String,
    configure: Button.() -> Unit = {},
): Button {
    val r = Rectangle(
        currentX,
        currentY,
        currentElementWidth,
        currentElementHeight
    )
    val b = Button(label, r, configure)
    add(b)
    return b
}
