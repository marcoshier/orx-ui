package demo

import UI
import elements.slider
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.shape.Rectangle

fun main() {
    application {
        configure {
            width = 320
            height = 240
        }

        program {
            extend(UI())

            var state = object {
                var v = 0.0
            }

            slider(Rectangle.fromCenter(drawer.bounds.center, 300.0, 200.0), 0.0..10.0, state::v) {
                style.strokeColor = ColorRGBa.GREEN
                afterDraw = {
                    drawer.stroke = ColorRGBa.RED
                    drawer.fill = null
                    drawer.rectangle(this@slider.bounds)
                }
            }
        }
    }
}