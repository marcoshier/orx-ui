package demo

import UI
import elements.button
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.LIGHT_SALMON
import org.openrndr.math.Vector2
import widgets.Direction
import widgets.widget

fun main() {
    application {
        configure {
            width = 480
            height = 960
        }
        program {
            extend(UI())

           // widget(drawer.bounds) {
            // widgets should be nestable and the hit testing for scroll
            // should be done the same as with mouse move
            // children should inherit parent properties
                widget(drawer.bounds.scaledBy(1.0, 0.5, 0.5, 0.0)) {
                    minElementHeight = 80.0
                    marginX = 10.0
                    marginY = 10.0
                    gutterY = 10.0
                    for (i in 0 until 12) {
                        append(Direction.DOWN) { button("A$i") }
                    }
                }

                widget(drawer.bounds.scaledBy(1.0, 0.5, 0.5, 1.0)) {
                    minElementHeight = 80.0
                    marginX = 10.0
                    marginY = 10.0
                    gutterY = 10.0

                    for (i in 0 until 12) {
                        append(Direction.DOWN) {
                            button("B$i") {
                                idleColor = ColorRGBa.LIGHT_SALMON
                            }
                        }
                    }
                }
          //  }
        }
    }
}