package demo

import UI
import org.openrndr.application
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import widgets.BoxSizing
import widgets.gridSlots
import widgets.transformWidget
import widgets.widget


fun main() {
    application {
        configure {
            width = 1200
            height = 650
        }

        program {
            extend(UI())

            val state = object {
                var tr0 = Matrix44.IDENTITY
                var tr1 = Matrix44.IDENTITY
            }

            widget(Rectangle(0.0, 0.0, width * 1.0, 600.0)) {
                gridSlots(2, 1, gutterX = 5.0, marginX = 5.0, marginY = 5.0) { r ->
                    transformWidget("tr0", r[0], state::tr0,
                        Vector2(-5.0)..Vector2(5.0),
                        Vector2.ONE..Vector2(5.0)
                    )
                    transformWidget("tr0", r[1], state::tr1,
                        Vector2(-5.0)..Vector2(5.0),
                        Vector2.ONE..Vector2(5.0)
                    ) { boxSizing = BoxSizing.BORDER_BOX }
                }
            }
        }
    }
}
