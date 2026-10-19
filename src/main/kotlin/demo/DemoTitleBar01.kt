package demo

import UI
import elements.button
import elements.titleBar
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.DARK_SALMON
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import style.Colors
import widgets.titleBarWidget
import widget.widget

fun main() = application {
    configure {
        hideWindowDecorations = true
    }
    program {
        extend(UI())

        titleBarWidget("OPENRNDR",
            drawer.bounds.copy(height = 30.0),
            windowTitlebar = true,
            zIndex = 10
        ) {
            button(Rectangle(bounds.width - 30.0, 5.0, 20.0)) {
                label = ""
                style.backgroundColor = Colors.CLICKABLE.shade(0.9)
                action = { application.exit() }
            }
        }

        widget(drawer.bounds.offsetEdges(-80.0).movedBy(Vector2(0.0, 30.0))) {
            this.backgroundColor = ColorRGBa.DARK_SALMON
            this.strokeColor = null
            titleBar("TITLEBAR-WIDGET", bounds.copy(height = 20.0), widgetTitlebar = true)
        }
    }

}


