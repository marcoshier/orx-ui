import elements.button
import elements.titleBar
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_SALMON
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import widget.widget

fun main() = application {
    configure {
        hideWindowDecorations = true
    }
    program {
        extend(UI())

        val t = titleBar("WINDOW TITLE BAR", drawer.bounds.copy(height = 30.0), windowTitlebar = true)
        val b = button(Rectangle(t.bounds.width - 25.0, 5.0, 20.0)) {
            label = ""
            action = { window.close() }
        }
        t.dragMasks = listOf(b.bounds)

        widget(drawer.bounds.offsetEdges(-80.0).movedBy(Vector2(0.0, 30.0))) {
            this.backgroundColor = ColorRGBa.DARK_SALMON
            this.strokeColor = null
            titleBar("TITLE BAR", bounds.copy(height = 20.0), widgetTitlebar = true)
        }
    }

}


