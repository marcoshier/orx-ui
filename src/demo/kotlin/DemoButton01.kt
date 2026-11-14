import elements.button
import org.openrndr.application
import org.openrndr.shape.Rectangle

fun main() = application {
    program {
        extend(UI())

        button { action = { println("clicked") } }
        button(Rectangle(width - 100.0, height - 40.0, 90.0, 30.0)) { action = { println("clicked 2") } }
    }
}