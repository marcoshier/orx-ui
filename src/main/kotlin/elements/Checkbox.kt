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
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0

class Checkbox(
    fullBounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {}
): Button(fullBounds.copy(width = fullBounds.height), configure) {

    init {
        label = ""
        beforeDraw = { isSelected = state.get() }
        action = { state.set(!state.get()) }
        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible) return

        val bgFill = when {
            isHovered && !isSelected -> effectiveStyle.hoverColor
            else -> effectiveStyle.backgroundColor
        }

        drawer.strokeWeight = 0.01
        drawer.stroke = effectiveStyle.strokeColor
        drawer.fill = bgFill

        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(bounds.toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        val mainFill = if (isSelected) effectiveStyle.selectColor else ColorRGBa.TRANSPARENT

        drawer.fill = mainFill
        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(bounds.offsetEdges(-3.0).toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(bounds.offsetEdges(-3.0))
        }

        if (label.isEmpty()) {
            drawer.isolated {
                if (isFocused) {
                    iconOn?.invoke(drawer)
                } else {
                    iconOff?.invoke(drawer)
                }
            }
        } else {
            drawer.fill = effectiveStyle.textColor
            drawer.fontMap = effectiveStyle.font

            val textBounds = drawer.writer {
                box = bounds.copy(width = 9999.0)
                horizontalAlign = textAlign.x
                verticalAlign = textAlign.y
                text(label, false)
                glyphOutput.rectangles.map { it.second }.bounds
            }

            if (textBounds.width > bounds.width) {
                drawer.isolated {
                    drawer.drawStyle.clip = this@Checkbox.bounds
                    drawer.writer {
                        box = this@Checkbox.bounds.copy(
                            corner = Vector2(this@Checkbox.bounds.x + 5.0, this@Checkbox.bounds.y),
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
                    box = this@Checkbox.bounds
                    horizontalAlign = textAlign.x
                    verticalAlign = textAlign.y
                    text(label)
                }
            }
        }

    }
}

fun checkbox(
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(bounds, state, configure)
    registerElement(c)
    return c
}

fun WidgetBuilder.checkbox(
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(bounds, state, configure)
    add(c)
    return c
}