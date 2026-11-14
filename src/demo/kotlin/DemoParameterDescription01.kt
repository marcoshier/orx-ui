import elements.button
import org.openrndr.application

fun main() {
    application {
        program {
            extend(UI())

            button(drawer.bounds.offsetEdges(-200.0)) {
                description = "This is the hovered element's description"
            }
        }
    }
}