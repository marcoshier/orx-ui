import elements.button
import org.openrndr.application
import widget.asRadio
import widget.gridIndexed
import widget.widget

fun main() {
    application {
        configure {
            width = 330
            height = 120
        }
        program {
            extend(UI())

            widget(drawer.bounds.offsetEdges(-10.0)) {
                asRadio {
                    gridIndexed(3, 1) { i, r ->
                        button(r.offsetEdges(-10.0)) {
                            label = "R$i"
                            style.borderRadius = 100.0
                        }
                    }
                }
            }
        }
    }
}