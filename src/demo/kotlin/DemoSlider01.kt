import elements.slider
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.shape.Rectangle

fun main() {
    application {
        configure {
            width = 320
            height = 240
        }

        program {
            extend(UI())

            val state = object {
                var d = 0.0
                var i = 5
            }

            val r = drawer.bounds.offsetEdges(-10.0)

            slider(0.0..10.0, state::d, r)
            slider(0..10, state::i, r)
        }
    }
}