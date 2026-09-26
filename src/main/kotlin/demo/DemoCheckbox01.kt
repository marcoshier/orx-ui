package demo

import UI
import elements.checkbox
import org.openrndr.application
import widgets.grid
import widgets.widget

fun main() {
    application {
        configure {
            width = 480
            height = 480
        }
        program {
            extend(UI())

            val states = (0..3).map {
                object { var on = false }
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(2, 2, gutterX = 20.0, gutterY = 20.0) { i, box ->
                    checkbox("", box, states[i]::on)
                }
            }
        }
    }
}