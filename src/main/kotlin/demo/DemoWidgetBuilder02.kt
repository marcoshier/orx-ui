package demo

import UI
import elements.Button
import elements.button
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import widgets.widget
import widgets.gridIndexed
import widgets.gridSlots

fun main() {
    application {
        configure {
            width = 1280
            height = 720
        }

        program {
            extend(UI())

            val grid = drawer.bounds.grid(2, 1, gutterX = 5.0).flatten()

            val configure: Button.() -> Unit = {
                action = { println("hello $label") }
            }

            widget(grid[0]) {
                gridSlots(1, 2, gutterX = 5.0, gutterY = 5.0) { slots ->
                    button(slots[0], configure)
                    button(slots[1], configure)
                }
            }

            widget(grid[1]) {
                gridIndexed(2, 2, gutterX = 5.0, gutterY = 5.0) { i, r ->
                    button(r) {
                        configure()
                        label = "$i"
                    }
                }
            }
            
        }
    }
}