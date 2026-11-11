import elements.rangeSlider
import elements.slider
import org.openrndr.application
import widget.grid
import widget.widget

fun main() {
    application {
        program {
            extend(UI())

            val state = object {
                var x0 = 0.0
                var x1 = 0.0
                var x2: ClosedRange<Double> = 0.0..5.0
            }

            widget(drawer.bounds) {
                grid(1, 3) {
                    slider(it[0], 0.0..5.0, state::x0) { precision = 1 }
                    slider(it[1], 0.0..5.0, state::x1) { precision = 2 }
                    rangeSlider(it[2], 0.0..5.0, state::x2) { precision = 3 }
                }
            }

        }
    }
}