package elements

import org.openrndr.Hit
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import registerElement
import style.Colors
import style.Fonts
import ui.UIElementImpl
import widgets.WidgetBuilder
import window

class TitleBar(
    override var label: String,
    override var bounds: Rectangle,
    var windowTitlebar: Boolean = false,
    var widgetTitlebar: Boolean = false,
    configure: TitleBar.() -> Unit = {}
): UIElementImpl(label, bounds) {

    override var fixed = !widgetTitlebar


    var backgroundColor = Colors.CLICKABLE

    var dragMasks = listOf<Rectangle>()
    val icon: (TitleBar.() -> Unit)? = null
    var font = Fonts.DEFAULT

    init {
        interactable = widgetTitlebar

        var locked = false
        var dragPosition = Vector2.ZERO

        buttonDown.listen {
            it.cancelPropagation()
            locked = true
            dragPosition = if (widgetTitlebar) {
                it.position
            } else {
                val w = window()
                it.position + w.position
            }
        }

        dragged.listen {
            if (!locked) return@listen
            if (widgetTitlebar) {
                val p = parent
                if (p != null) {
                    val delta = it.position - dragPosition
                    p.bounds = p.bounds.movedBy(delta)
                    this@TitleBar.bounds = this@TitleBar.bounds.movedBy(delta)
                    dragPosition = it.position
                }
            } else {
                val w = window()
                val screen = it.position + w.position
                w.position += (screen - dragPosition)
                dragPosition = screen
            }
        }

        buttonUp.listen {
            locked = false
            dragPosition = Vector2.ZERO
        }

        if (windowTitlebar) {
            val w = window()
            w.hitTest = { p ->
                val scaled = p / w.contentScale
                if (scaled in bounds && dragMasks.none { mask -> mask.contains(scaled) })
                    Hit.DRAG
                else Hit.NORMAL
            }
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)

        drawer.isolated {
            val bb = this@TitleBar.bounds

            drawer.stroke = null
            drawer.fill = backgroundColor
            drawer.rectangle(bb.x, bb.y, bb.width, bb.height)

            drawer.fill = ColorRGBa.BLACK
            drawer.fontMap = font

            if (icon != null) {
                icon()
            }

            if (icon != null) {
                drawer.translate(bb.height * 0.9, 0.0)
            }

            drawer.translate(10.0, 0.0)
            drawer.writer {
                box = bb
                horizontalAlign = 0.0
                verticalAlign = 0.5
                text(label.uppercase())
            }
        }
    }

}

fun titleBar(
    label: String,
    bounds: Rectangle,
    windowTitlebar: Boolean = false,
    widgetTitlebar: Boolean = false,
    configure: TitleBar.() -> Unit = {},
): TitleBar {
    val b = TitleBar(label, bounds, windowTitlebar, widgetTitlebar, configure)
    registerElement(b)
    return b
}

fun WidgetBuilder.titleBar(
    label: String,
    bounds: Rectangle,
    windowTitlebar: Boolean = false,
    widgetTitlebar: Boolean = false,
    configure: TitleBar.() -> Unit = {},
): TitleBar {
    val b = TitleBar(label, bounds, windowTitlebar,  widgetTitlebar, configure)
    add(b)
    return b
}
