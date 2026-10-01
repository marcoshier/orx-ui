package demo

import UI
import elements.xyControl
import org.openrndr.application
import org.openrndr.math.Vector2

fun main() {
    application {
        configure {
            width = 480
            height = 480
        }

        program {
            extend(UI())

            val state = object {
                var position = Vector2.ZERO
            }

            xyControl("",
                drawer.bounds.offsetEdges(-70.0),
                state::position, -100.0..100.0, -100.0..100.0
            )
        }
    }
}