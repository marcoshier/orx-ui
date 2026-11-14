import elements.button
import org.openrndr.application
import widget.grid
import widget.widget

fun main(){
    application {
        program {
            extend(UI())


            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(4, 3, gutterX = 10.0, gutterY = 10.0) {
                    for (i in 0 until it.size) {
                        button(it[i]) { action = { println("click ${i + 1}") } }
                    }
                }
            }
        }
    }
}