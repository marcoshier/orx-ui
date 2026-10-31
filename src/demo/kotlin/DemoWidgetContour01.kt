import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.shapes.uniform
import org.openrndr.shape.ShapeContour
import widgets.contourWidget


fun main() {
    application {
        program {
            extend(UI())

            val points = (0..4).map { drawer.bounds.uniform() }

            val state = object {
                var contour = ShapeContour.fromPoints(points, false)
            }

            contourWidget(state::contour)


            extend {
                drawer.strokeWeight = 3.0
                drawer.stroke = ColorRGBa.RED
                drawer.fill = null
                drawer.contour(state.contour)
            }



        }
    }
}