import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.defaultFontMap
import org.openrndr.extra.color.presets.ORANGE_RED
import org.openrndr.extra.noise.shapes.uniform
import org.openrndr.extra.textwriter.writer
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
                drawer.stroke = ColorRGBa.ORANGE_RED
                drawer.fill = null
                drawer.contour(state.contour)

                drawer.fill = ColorRGBa.WHITE
                drawer.fontMap = defaultFontMap
                drawer.writer {
                    box = drawer.bounds.offsetEdges(-10.0)
                    horizontalAlign = 0.0
                    verticalAlign = 1.0
                    text("Shift+CLICK on a line to add a point.\n" +
                            "Alt+CLICK on a vertex to make it cubic.\n" +
                            "Ctrl+CLICK on a vertex to remove it\n" +
                            "Shift while holding a control point to transform symmetrically.")
                }
            }

        }
    }
}