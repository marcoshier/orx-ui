import elements.slider
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.shape.Rectangle

fun main() {
    application {
        configure {
            width = 480
            height = 120
        }

        program {
            extend(UI())

            val state = object {
                var d = 0.0
                var i = 5
            }

            val r = drawer.bounds.offsetEdges(-10.0).grid(1, 2).flatten()

            slider(0.0..10.0, state::d, r[0])
            slider(0..10, state::i, r[1])
        }
    }
}