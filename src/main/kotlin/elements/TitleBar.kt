package elements

import org.openrndr.Hit
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import registerElement
import ui.UIElement
import widget.WidgetBuilder
import window

class TitleBar(
    override var label: String,
    bounds: Rectangle,
    var windowTitlebar: Boolean = false,
    var widgetTitlebar: Boolean = false,
    configure: TitleBar.() -> Unit = {}
): UIElement(bounds) {

    var dragMasks = listOf<Rectangle>()
    val icon: (TitleBar.() -> Unit)? = null

    init {
        interactable = widgetTitlebar
        fixed = true

        var locked = false
        var dragPosition = Vector2.ZERO

        buttonDown.listen {
            it.cancelPropagation()
            locked = true
            dragPosition = if (widgetTitlebar) {
                val p = parent
                it.position + Vector2(xOffset, yOffset) + (p?.dragOffset ?: Vector2.ZERO)
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
                    val screenPos = it.position + Vector2(xOffset, yOffset) + p.dragOffset

                    val delta = screenPos - dragPosition
                    p.dragOffset += delta
                    dragPosition = screenPos
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
        if (!visible) return

        parent?.elements?.maxOfOrNull { it.zIndex }?.let {
            zIndex = it + 1
        }

        drawer.isolated {
            val bb = this@TitleBar.bounds

            drawer.stroke = null
            drawer.fill = style.backgroundColor
            drawer.rectangle(bb.x, bb.y, bb.width, bb.height)

            drawer.fill = ColorRGBa.BLACK
            drawer.fontMap = effectiveStyle.font

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
