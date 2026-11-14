import elements.button
import elements.dropdown
import elements.slider
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import widget.widget

private enum class Test {
    OPTION_0, OPTION_1, OPTION_2, OPTION_3
}

fun main() {
    application {
        configure {
            width = 720
            height = 480
        }

        program {
            extend(UI())

            val grid = drawer.bounds.offsetEdges(-60.0).grid(1, 4, gutterY = 10.0).flatten()

            val state = object {
                var current0 = Test.OPTION_0
                var current1 = Test.OPTION_0
                var value = 0.0
            }

            widget(drawer.bounds) {
                dropdown<Test>(state::current0, grid[0])
                dropdown<Test>(state::current1, grid[1])

                button(grid[2]) {
                    action = { println("clicked button") }
                }

                slider(0.0..10.0, state::value, grid[3])
            }
        }
    }
}