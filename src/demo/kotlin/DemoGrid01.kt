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
                    button(it[0], state::a)
                    button(it[1], state::b)
                    button(it[2], state::c)
                    button(it[3], state::d)
                }
            }
        }
    }
}