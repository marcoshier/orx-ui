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
import registerElement
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.reflect.KMutableProperty0

open class Button(
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
) : UIElementImpl(bounds) {

    var action: (MouseEvent) -> Unit = {}
    var dragAction: (MouseEvent) -> Unit = {}

    var iconOff: (Drawer.() -> Unit)? = null
        set(value) {
            field = value
            if (iconOn == null) iconOn = field
        }

    var iconOn: (Drawer.() -> Unit)? = null

    var textAlign = Vector2(0.5)

    var buttonDownPosition = Vector2.ZERO

    init {
        buttonDown.listen {
            it.cancelPropagation()
            buttonDownPosition = it.position
            action(it)
        }
        dragged.listen { dragAction(it) }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible) return

        val mainFill = when {
            isHovered && !isSelected -> style.hoverColor
            isFocused && !isSelected -> style.focusColor
            isHovered && isSelected -> style.selectColor?.shade(0.75)
            isSelected -> style.selectColor
            else -> style.backgroundColor
        }

        drawer.strokeWeight = 0.01
        drawer.stroke = style.strokeColor
        drawer.fill  = mainFill

        if (style.borderRadius > 0.0) {
            drawer.roundedRectangle(bounds.toRounded(style.borderRadius))
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
            drawer.fill = style.textColor
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
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(bounds, configure)
    registerElement(b)
    return b
}

fun button(
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
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
    bounds: Rectangle,
    configure: Button.() -> Unit = {},
): Button {
    val b = Button(bounds, configure)
    add(b)
    return b
}

fun WidgetBuilder.button(
    bounds: Rectangle,
    state: KMutableProperty0<Boolean>,
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
    val r = Rectangle(
        itemX,
        itemY,
        itemWidth,
        itemHeight
    )
    val b = Button(r, configure)
    add(b)
    return b
}
