import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.imageFit.FitMethod
import org.openrndr.extra.imageFit.fitRectangle
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import widget.grid
import widgets.transformWidget
import widget.widget


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

            var rects = listOf<Rectangle>()

            widget(Rectangle(0.0, 0.0, width * 1.0, 600.0)) {
                grid(2, 1, gutterX = 5.0, marginX = 5.0, marginY = 5.0) { r ->
                    transformWidget(r[0], state::tr0,
                        Vector2(-5.0)..Vector2(5.0),
                        Vector2.ONE..Vector2(5.0)
                    ) { precision = 3 }

                    transformWidget(r[1], state::tr1,
                        Vector2(-5.0)..Vector2(5.0),
                        Vector2.ONE..Vector2(5.0)
                    ) {
                        fitMethod = FitMethod.Contain
                    }

                    rects = r
                }
            }

            extend {
                drawer.stroke = ColorRGBa.GREEN
                drawer.fill = null
                drawer.rectangles(rects)
            }
        }
    }
}
