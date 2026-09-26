package demo

import UI
import elements.Button
import elements.button
import elements.dropdown
import elements.slider
import org.openrndr.application
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.shape.Rectangle
import widgets.asCheckbox
import widgets.asRadio
import widgets.widget

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }

        program {
            extend(UI())

            val grid = drawer.bounds.offsetEdges(-100.0).grid(2, 2, gutterX = 10.0, gutterY = 10.0, ).flatten()

            class RoundButton(
                override var label: String,
                override var bounds: Rectangle
            ): Button(label, bounds, { borderRadius = 20.0 })

            widget(drawer.bounds) {
                asCheckbox {
                    add(RoundButton("1", grid[0]))
                    add(RoundButton("2", grid[1]))
                }

                listOf(button("3", grid[2]), button("4", grid[3])).asRadio()
            }
        }
    }
}