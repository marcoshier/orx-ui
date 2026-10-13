package demo

import UI
import elements.rotateControl
import org.openrndr.application

fun main() {
    application {
        configure {
            width = 480
            height = 480
        }

        program {
            extend(UI())

            val state = object {
                var angle = 0.0
            }

            rotateControl(drawer.bounds.offsetEdges(-70.0), state::angle)

        }
    }
}