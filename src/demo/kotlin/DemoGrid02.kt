import elements.button
import org.openrndr.application
import widget.grid
import widget.gridIndexed
import widget.widget

fun main(){
    application {
        program {
            extend(UI())

            val states = (0..3).map {
                object { var x = false }
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                gridIndexed(30.0, 30.0, 0.0, 0.0, 5.0, 5.0) { i, r ->
                    button(r, states[i]::x)
                }
            }

        }
    }
}