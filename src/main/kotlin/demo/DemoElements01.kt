package demo

import UI
import elements.button
import elements.dropdown
import elements.slider
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.shape.Rectangle
import widgets.widget

private enum class Test {
    OPTION_0, OPTION_1, OPTION_2, OPTION_3
}

fun main() {
    application {
        configure {
            width = 1280
            height = 720
        }

        program {
            extend(UI())

            val grid = drawer.bounds.offsetEdges(-100.0).grid(1, 4, gutterY = 10.0).flatten()

            val state = object {
                var current0 = Test.OPTION_0
                var current1 = Test.OPTION_0
                var value = 0.0
            }

            widget(drawer.bounds) {
                button("click me", grid[0]) {
                    action = { println("clicked button") }
                }

                dropdown<Test>("test 0", grid[1], state::current0)
                dropdown(
                    "test 1",
                    grid[2],
                    entries = { Test.entries.toList() },
                    entryName = { it.name },
                    current = state::current1,
                )

                slider("slider", grid[3], 0.0..10.0, state::value)

                button("center", Rectangle.fromCenter(drawer.bounds.center, 100.0)) {
                    action = { println("centered") }
                }
            }

            extend {

            }
        }
    }
}