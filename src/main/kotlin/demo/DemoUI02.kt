package demo

import UI
import elements.selectableMutableListOf
import lib.DoubleRangeParameter
import lib.Matrix44Parameter
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.noise.multiScatter
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.MutableListParameter
import org.openrndr.math.Matrix44
import org.openrndr.shape.Circle
import ui.fromParameters

fun main() {
    application {
        program {

            fun generate(): List<Circle> {
                val points = drawer.bounds.multiScatter(
                    (0..50).map {
                        val or = Double.uniform(5.0, 35.0)
                        or to or * 0.75
                    }
                )

                return points.flatMap { (r, p) ->
                    p.map { Circle(it, r * Double.uniform(0.85, 1.15)) }
                }
            }

            val settings = object {

                @MutableListParameter("sets")
                var sets = selectableMutableListOf(generate())

                @DoubleRangeParameter("radius range", 5.0, 35.0)
                var radiusRange = 15.0..25.0

                @ActionParameter("generate set")
                fun addSet() {
                    sets.add(generate())
                    sets.selectedIndex = sets.lastIndex
                }
                
                @Matrix44Parameter("camera view",
                    -100.0, 100.0, -100.0, 100.0,
                    1.0, 3.0, -360.0, 360.0
                )
                var view = Matrix44.IDENTITY

            }

            extend(UI()).fromParameters(settings)
            val c = extend(Camera2D())
            extend {

                c.view = settings.view

                drawer.stroke = null
                drawer.fill = ColorRGBa.WHITE

                val filtered = settings.sets.selected?.filter {
                    it.radius in settings.radiusRange
                } ?: listOf()

                drawer.circles(filtered)

            }
        }
    }
}

