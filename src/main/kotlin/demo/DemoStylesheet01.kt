package demo

import UI
import elements.button
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.ORANGE_RED
import style.style
import widgets.grid
import widgets.gridSlots
import widgets.widget
import java.io.File

fun main() {
    application {
        program {
            extend(UI())

            val style = style {
                "high-contrast" {
                    hoverColor = null
                    borderRadius = 0.0
                    backgroundColor = ColorRGBa.BLACK
                    textColor = ColorRGBa.WHITE
                    strokeColor = ColorRGBa.WHITE
                    strokeWeight = 1.0
                    transition(::backgroundColor, 0.5, Easing.SineInOut)
                    transition(::textColor, 0.0, Easing.SineInOut)
                }

                "high-contrast:hover" {
                    backgroundColor = ColorRGBa.WHITE
                    textColor = ColorRGBa.BLACK
                }
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(2, 2, gutterX = 10.0, gutterY = 10.0) {
                    button(it) {
                        selectors = setOf("high-contrast")
                    }
                }
            }

            style.toFile(File("data/style/demo-stylesheet-01.conf"))

        }
    }
}


