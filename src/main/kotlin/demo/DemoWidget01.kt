package demo

import UI
import elements.button
import org.openrndr.application
import org.openrndr.shape.Rectangle
import widgets.widget

fun main() {
    application {
        program {
            extend(UI())

            widget(drawer.bounds) {
                button(Rectangle(10.0, 10.0, 100.0)) {
                    action = { println("clicked") }
                }

                button(Rectangle(40.0, 40.0, 100.0)) {
                    action = { println("clicked 2") }
                }
            }

            extend {

            }
        }
    }
}