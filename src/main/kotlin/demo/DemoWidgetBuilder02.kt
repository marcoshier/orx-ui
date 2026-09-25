package demo

import UI
import elements.Button
import elements.button
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import widgets.widget
import widgets.grid
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

            val conf: Button.() -> Unit = {
                action = { println("hello $label") }
            }

            widget(grid[0]) {
                gridSlots(1, 2, gutterX = 5.0, gutterY = 5.0) { slots ->
                    button("ZERO", slots[0], conf)
                    button("ONE", slots[1], conf)
                }
            }

            widget(grid[1]) {
                grid(2, 2, gutterX = 5.0, gutterY = 5.0) { i, r ->
                    button("$i", r, conf)
                }
            }

            extend {

            }
        }
    }
}