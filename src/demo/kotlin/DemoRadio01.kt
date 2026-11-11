import elements.button
import org.openrndr.application
import org.openrndr.shape.Rectangle
import widget.asRadio
import widget.widget

// TODO does not work? or display does not work

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
                asRadio {
                    states.mapIndexed { i, state ->
                        val r = Rectangle(i * 120.0, 0.0, 120.0).offsetEdges(-10.0)
                        button(state::on, r) { label = "" }
                    }
                }
            }
        }
    }
}