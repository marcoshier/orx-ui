package demo

import UI
import elements.button
import org.openrndr.application
import widget.grid
import widget.widget
import java.io.File

fun main() {
    application {
        program {
            val ui = extend(UI())
            ui.loadStylesheet(File("data/style/demo-stylesheet-01.conf"))

            widget(drawer.bounds.offsetEdges(-10.0)) {
                grid(2, 2, gutterX = 10.0, gutterY = 10.0) {
                    button(it) {
                        selectors = setOf("high-contrast")
                    }
                }
            }
        }
    }
}


