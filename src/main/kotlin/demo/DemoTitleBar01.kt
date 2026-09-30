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
import widgets.widget

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
            button("X", Rectangle(bounds.width - 30.0, 5.0, 20.0)) {
                style.idleColor = Colors.CLICKABLE.shade(0.9)
                action = { application.exit() }
            }
        }

        widget(drawer.bounds.offsetEdges(-80.0).movedBy(Vector2(0.0, 30.0))) {
            this.background = ColorRGBa.DARK_SALMON
            titleBar("WIDGET", bounds.copy(height = 20.0), widgetTitlebar = true)
        }
    }
}


