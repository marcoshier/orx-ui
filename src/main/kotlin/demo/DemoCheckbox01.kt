package demo

import UI
import elements.checkbox
import org.openrndr.application
import widget.gridIndexed
import widget.widget

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
                gridIndexed(2, 2, gutterX = 10.0, gutterY = 10.0) { i, box ->
                    checkbox(box, states[i]::on)
                }
            }
        }
    }
}