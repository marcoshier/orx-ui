package demo

import UI
import elements.button
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import widgets.grid
import widgets.widget

fun main() {
    application {
        program {
            extend(UI())

            widget(drawer.bounds) {
                val grid = bounds.grid(1, 2, gutterY = 10.0).flatten()

                widget(grid[0]) {
                    grid(2, 1, gutterX = 10.0) { i, r ->
                        button("AA$i", r)
                    }
                }
                widget(grid[1]) {
                    grid(2, 1, gutterX = 10.0) { i, r ->
                        button("BB$i", r)
                    }
                }


            }
        }
    }
}