package demo

import UI
import elements.button
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.ORANGE_RED
import style.StyleModifier
import style.style
import widgets.grid
import widgets.gridSlots
import widgets.widget
import java.io.File

fun main() {
    application {
        program {
            extend(UI())

            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(2, 2, gutterX = 10.0, gutterY = 10.0) {
                    button(it) { selectors = setOf("round") }
                }
            }

            style {
                "round" {
                    hoverColor = null
                    transition(0.5, Easing.SineInOut)
                }

                "round:hover" {
                    backgroundColor = ColorRGBa.ORANGE_RED
                    borderRadius = 200.0
                }
            }
        }
    }
}


