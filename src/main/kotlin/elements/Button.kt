package elements

import org.openrndr.MouseEvent
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import ui.registerElement
import ui.UIElement
import ui.applyBoundsFromText
import widget.WidgetBuilder
import kotlin.reflect.KMutableProperty0

open class Button(
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
) : UIElement(bounds ?: Rectangle(10.0, 10.0, 30.0)) {

    var action: (MouseEvent) -> Unit = {}
    var dragAction: (MouseEvent) -> Unit = {}

    var iconOff: (Drawer.() -> Unit)? = null
        set(value) {
            field = value
            if (iconOn == null) iconOn = field
        }
    var iconOn: (Drawer.() -> Unit)? = null

    var textAlign = Vector2(0.5)

    init {
        buttonDown.listen {
            it.cancelPropagation()
            action(it)
            isActive = true
        }

        buttonUp.listen {
            isActive = false
        }

        dragged.listen {
            isActive = true
            dragAction(it)
        }

        if (bounds == null) {
            applyBoundsFromText()
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible()) return

        drawer.strokeWeight = effectiveStyle.strokeWeight
        drawer.stroke = effectiveStyle.strokeColor
        drawer.fill = effectiveStyle.backgroundColor

        if (effectiveStyle.borderRadius > 0.0) {
            drawer.roundedRectangle(bounds.toRounded(effectiveStyle.borderRadius))
        } else {
            drawer.rectangle(bounds)
        }

        drawer.strokeWeight = 1.0


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
                    box = this@Button.bounds
                    horizontalAlign = textAlign.x
                    verticalAlign = textAlign.y
                    text(label)
                }
            }
        }

    }
}


fun button(
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(bounds, configure)
    registerElement(b)
    return b
}

fun button(
    state: KMutableProperty0<Boolean>,
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(bounds) {
        action = { state.set(!state.get()) }
        beforeDraw = { isFocused = state.get() }
        configure()
    }
    registerElement(b)
    return b
}

fun WidgetBuilder.button(
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(bounds, configure)
    add(b)
    return b
}

fun WidgetBuilder.button(
    state: KMutableProperty0<Boolean>,
    bounds: Rectangle? = null,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(bounds) {
        action = { state.set(!state.get()) }
        beforeDraw = { isSelected = state.get() }
        configure()
    }
    add(b)
    return b
}

fun WidgetBuilder.button(
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(currentElementBounds, configure)
    add(b)
    return b
}
