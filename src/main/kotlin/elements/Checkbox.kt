package elements

import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import ui.registerElement
import widget.WidgetBuilder
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class Checkbox(
    val getter: () -> Boolean,
    val setter: (Boolean) -> Unit,
    fullBounds: Rectangle? = null,
    configure: Button.() -> Unit = {}
): Button(fullBounds ?: Rectangle(10.0, 10.0, 30.0), configure) {

    constructor(
        state: KMutableProperty0<Boolean>,
        fullBounds: Rectangle? = null,
        configure: Button.() -> Unit = {},
    ) : this({ state.get() }, { state.set(it) }, fullBounds, configure)

    constructor(
        obj: Any,
        valueRef: KMutableProperty1<Any, Boolean>,
        fullBounds: Rectangle? = null,
        configure: Button.() -> Unit = {}
    ) : this({ valueRef.get(obj) }, { valueRef.set(obj, it) }, fullBounds, configure)

    init {
        label = ""
        beforeDraw = { isSelected = getter() }
        action = { setter(!getter()) }

        configure()
    }

    override fun draw(drawer: Drawer) {
        if (!visible()) return

        drawer.strokeWeight = 0.01
        drawer.stroke = effectiveStyle.strokeColor
        drawer.fill = effectiveStyle.backgroundColor

        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(bounds.toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        drawer.fill = effectiveStyle.checkboxBackgroundFill
        drawer.stroke = effectiveStyle.checkboxStroke
        val b = bounds.offsetEdges(-bounds.width * 0.1)

        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(b.toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(b)
        }

        if (iconOn != null || iconOff != null) {
            drawer.isolated {
                if (isFocused) {
                    iconOn?.invoke(drawer)
                } else {
                    iconOff?.invoke(drawer)
                }
            }
        } else {
            drawer.fill = effectiveStyle.textColor
            drawer.fontMap = effectiveStyle.fontMap

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
    state: KMutableProperty0<Boolean>,
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(state, bounds, configure)
    registerElement(c)
    return c
}

fun WidgetBuilder.checkbox(
    state: KMutableProperty0<Boolean>,
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(state, bounds, configure)
    add(c)
    return c
}

fun WidgetBuilder.checkbox(
    obj: Any,
    valueRef: KMutableProperty1<Any, Boolean>,
    configure: Button.() -> Unit = {},
): Checkbox {
    val c = Checkbox(obj, valueRef, currentElementBounds, configure)
    add(c)
    return c
}