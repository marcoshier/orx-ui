import elements.button
import org.openrndr.application
import widget.grid
import widget.widget

fun main(){
    application {
        program {
            extend(UI())

            val state = object {
                var a = false
                var b = false
                var c = false
                var d = true
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(4, 3, gutterX = 10.0, gutterY = 10.0) {
                    button(state::a, it[0])
                    button(state::b, it[1])
                    button(state::c, it[2])
                    button(state::d, it[3])
                }
            }
        }
    }
}