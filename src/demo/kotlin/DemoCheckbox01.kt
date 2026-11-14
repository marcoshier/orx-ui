import elements.checkbox
import org.openrndr.application
import org.openrndr.shape.Rectangle
import ui.UIHost
import widget.widget

fun main() {
    application {
        configure {
            width = 480
            height = 120
        }
        program {
           extend(UI())

            val states = (0..3).map {
                object { var on = false }
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                for ((i, state) in states.withIndex()) {
                    val r = Rectangle(i * 120.0, 0.0, 120.0).offsetEdges(-10.0)
                    checkbox(state::on, r)
                }
            }

        }
    }
}