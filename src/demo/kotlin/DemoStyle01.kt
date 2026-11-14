import elements.button
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.ORANGE_RED
import style.stylesheet
import widget.gridIndexed
import widget.widget

fun main() {
    application {
        program {
            extend(UI())

            widget(drawer.bounds.offsetEdges(-10.0)) {
                gridIndexed(2, 2, gutterX = 10.0, gutterY = 10.0) { _, it ->
                    button(it) { selectors = setOf("round") }
                }
            }

            stylesheet {
                "round" {
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


