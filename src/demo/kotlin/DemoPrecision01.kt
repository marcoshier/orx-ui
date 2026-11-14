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
                grid(1, 3, 10.0, 10.0, 10.0, 10.0) {
                    slider(0.0..5.0, state::x0, it[0]) {
                        label = "slider - precision 1"
                        precision = 1 }
                    slider(0.0..5.0, state::x1, it[1]) {
                        label = "slider - precision 2"
                        precision = 2 }
                    rangeSlider(0.0..5.0, state::x2, it[2]) {
                        label = "slider - precision 3"
                        precision = 3
                    }
                }
            }

        }
    }
}