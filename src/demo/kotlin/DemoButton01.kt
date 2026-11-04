import elements.button
import org.openrndr.application

fun main() = application {
    program {
        extend(UI())

        button(drawer.bounds.offsetEdges(-10.0)) {
            action = { println("clicked") }
        }
    }
}