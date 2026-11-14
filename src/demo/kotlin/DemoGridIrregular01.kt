import elements.button
import org.openrndr.application
import widget.irregularGrid
import widget.widget

fun main(){
    application {
        program {
            extend(UI())

            val states = (0..8).map {
                object { var x = false }
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                irregularGrid(listOf(0.4, 0.3, 0.3), listOf(0.5, 0.4, 0.1)) { rects ->
                    for (i in 0..8) {
                        button(states[i]::x, rects[i].offsetEdges(-2.0))
                    }
                }
            }
        }
    }
}