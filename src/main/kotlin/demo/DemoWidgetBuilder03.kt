package demo

import UI
import elements.Button
import elements.button
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import widgets.asCheckbox
import widgets.widget
import widgets.grid
import widgets.gridSlots
import widgets.irregularGrid

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }

        program {
            extend(UI())

            widget(drawer.bounds.offsetEdges(-20.0)) {
                irregularGrid(listOf(0.2, 0.5, 0.3), listOf(0.1, 0.4, 0.5)) { rect ->
                    button("X", rect.offsetEdges(-5.0))
                }.asCheckbox()
            }
        }
    }
}