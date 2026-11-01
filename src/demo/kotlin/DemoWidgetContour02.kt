import lib.ContourParameter
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.shapes.uniform
import org.openrndr.shape.ShapeContour
import ui.fromParameters


fun main() {
    application {
        program {
            val points = (0..4).map { drawer.bounds.uniform() }

            val state = object {
                @ContourParameter("contour-01")
                var contour = ShapeContour.fromPoints(points, false)
            }

            extend(UI()).fromParameters(state)
            extend {
                drawer.strokeWeight = 3.0
                drawer.stroke = ColorRGBa.RED
                drawer.fill = null
                drawer.contour(state.contour)
            }
        }
    }
}