import elements.button
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.ORANGE_RED
import style.stylesheet
import widget.grid
import widget.widget

fun main() {
    application {
        program {
            val ui = extend(UI())

            ui.host.fontSizeStep = 1.0

            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(2, 2, gutterX = 10.0, gutterY = 10.0) {
                    button(it[0]) {
                        selectors = setOf("orange", "nf")
                    }
                    button(it[1]) {
                        selectors = setOf("orange", "round", "nf")
                    }
                    button(it[2]) {
                        selectors = setOf("big-label", "nf")
                    }
                    button(it[3]) {
                        selectors = setOf("big-label", "round", "nf")
                    }
                }
            }

            stylesheet {
                "orange" {
                    backgroundColor = ColorRGBa.ORANGE_RED
                }

                "round" {
                    borderRadius = 300.0
                }

                "big-label" {
                    transition(::fontSize, 0.5, Easing.SineInOut)
                }

                "big-label"(hover) {
                    fontSize = 30.0
                }
            }
        }
    }
}


