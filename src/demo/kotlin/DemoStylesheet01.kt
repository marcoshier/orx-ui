import elements.button
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import style.stylesheet
import widget.gridIndexed
import widget.widget
import java.io.File

fun main() {
    application {
        program {
            extend(UI())

            val style = stylesheet {
                "high-contrast" {
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
                gridIndexed(2, 2, gutterX = 10.0, gutterY = 10.0) { _, it ->
                    button(it) {
                        selectors = setOf("high-contrast")
                    }
                }
            }

            style.toFile(File("data/style/demo-stylesheet-01.conf"))

        }
    }
}


