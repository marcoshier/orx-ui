package demo

import UI
import elements.Button
import elements.button
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.shape.Rectangle
import widget.asCheckbox
import widget.asRadio
import widget.widget

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }

        program {
            extend(UI())

            val grid = drawer.bounds.offsetEdges(-100.0).grid(2, 2, gutterX = 10.0, gutterY = 10.0,).flatten()

            class RoundButton(bounds: Rectangle): Button(bounds, { style.borderRadius = 20.0; beforeDraw = { label = "${isSelected}" } })

            widget(drawer.bounds) {
                asCheckbox {
                    add(RoundButton(grid[0]))
                    add(RoundButton(grid[1]))
                }

                listOf(button(grid[2]), button(grid[3])).asRadio()
            }
        }
    }
}

