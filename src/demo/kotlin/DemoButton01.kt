import elements.button
import org.openrndr.application

fun main() = application {
    program {
        extend(UI())

        button { action = { println("clicked") } }

        button(drawer.bounds.offsetEdges(-200.0)) { action = { println("clicked 2") } }
    }
}