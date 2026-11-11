import elements.scaleControl
import org.openrndr.application
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.math.Vector2
import widget.grid
import widget.widget

fun main() {
    application {
        configure {
            width = 480
            height = 960
        }

        program {
            extend(UI())

            val state = object {
                var scale1 = 0.5
                var scale2 = Vector2(0.5)
            }

            widget(drawer.bounds.offsetEdges(-5.0)) {
                grid(1, 2) { rects ->
                    scaleControl(
                        state::scale1, 0.0..5.0,
                        rects[0].offsetEdges(-5.0)
                    )
                    scaleControl(
                        state::scale2, Vector2.ZERO..Vector2(3.0, 8.0),
                        rects[1].offsetEdges(-5.0)
                    )
                }
            }

        }
    }
}