package elements

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import registerElement
import style.Colors
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0

class Checkbox(
    label: String,
    val fullBounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {}
): Button(label, fullBounds.copy(width = fullBounds.height), configure) {

    init {
        action = { state.set(!state.get()) }
        beforeDraw = { isSelected = state.get() }
        configure()
    }

    override fun draw(drawer: Drawer) {
        beforeDraw()
        visible = visibleIf()
        if (!visible) { isFocused = false; return }

        val bgFill = when {
            isHovered && !isSelected -> hoverColor
            else -> idleColor
        }

        drawer.strokeWeight = 0.01
        drawer.stroke = if (border) Colors.BLACK else null
        drawer.fill = bgFill

        if (rounded) {
            drawer.roundedRectangle(bounds.toRounded(borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        val mainFill = when {
            isFocused || isSelected -> selectColor
            else -> ColorRGBa.TRANSPARENT
        }

        drawer.fill = mainFill
        if (rounded) {
            drawer.roundedRectangle(bounds.offsetEdges(-3.0).toRounded(borderRadius))
        } else {
            drawer.rectangle(bounds.offsetEdges(-3.0))
        }

        if (label.isNotEmpty()) {
            drawer.fill = textColor
            drawer.fontMap = font

            val textBox = Rectangle(
                bounds.x + bounds.height + 10.0,
                bounds.y,
                fullBounds.width - (bounds.height + 5.0),
                bounds.height
            )

            val textBounds = drawer.writer {
                box = textBox
                horizontalAlign = 0.0
                verticalAlign = textAlign.y
                text(label, false)
                glyphOutput.rectangles.map { it.second }.bounds
            }

            if (textBounds.width > fullBounds.width) {
                drawer.isolated {
                    drawer.drawStyle.clip = this@Checkbox.bounds
                    drawer.writer {
                        box = textBox
                        horizontalAlign = 0.0
                        verticalAlign = textAlign.y
                        text(label)
                    }
                    drawer.drawStyle.clip = null
                }
            } else {
                drawer.writer {
                    box = textBox
                    horizontalAlign = 0.0
                    verticalAlign = textAlign.y
                    text(label)
                }
            }

        }

    }
}

fun checkbox(
    label: String,
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(label, bounds, state, configure)
    registerElement(c)
    return c
}

fun WidgetBuilder.checkbox(
    label: String,
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(label, bounds, state, configure)
    add(c)
    return c
}