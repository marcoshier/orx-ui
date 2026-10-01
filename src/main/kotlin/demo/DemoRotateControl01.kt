package demo

import UI
import elements.rotateControl
import elements.scaleControl
import elements.xyControl
import org.openrndr.application
import org.openrndr.math.Vector2
import widgets.grid
import widgets.gridSlots
import widgets.widget

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

            rotateControl("", drawer.bounds.offsetEdges(-70.0), state::angle)

        }
    }
}