import elements.button
import org.openrndr.application
import widget.gridIndexed
import widget.widget

fun main(){
    application {
        configure {
            width = 700
            height = 700
        }
        program {
            extend(UI())

            val states = (0..100).map {
                object { var x = false }
            }

            widget(drawer.bounds) {
                gridIndexed(70.0, 70.0) { i, r ->
                    button(states[i]::x, r.offsetEdges(-2.0))
                }
            }

        }
    }
}