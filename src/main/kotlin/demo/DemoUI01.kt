package demo

import UI
import lib.IntVector3Parameter
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.camera.OrbitalManual
import org.openrndr.extra.camera.ProjectionType
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.OptionParameter
import org.openrndr.extra.parameters.Vector2Parameter
import org.openrndr.extra.parameters.Vector3Parameter
import org.openrndr.math.IntVector3
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import ui.fromParameters

fun main() {
    application {
        program {

            val orb = OrbitalManual().apply {
                this.userInteraction = false
                camera.projectionType = ProjectionType.ORTHOGONAL
            }

            val settings = object {
                @Vector3Parameter("boxSize", 1.0, 64.0)
                var boxSize = Vector3.ONE

                @IntVector3Parameter("sides", 1, 64)
                var sides = IntVector3.ONE

                @Vector2Parameter("rotation", 0.0, 360.0)
                var r = Vector2.ZERO

                @IntParameter("n", 1, 5)
                var n = 5

                @OptionParameter("draw type")
                var drawType = DrawPrimitive.TRIANGLES
            }


            extend(UI()).fromParameters(settings)
            extend(orb)
            extend {
                drawer.translate(25.0, 0.0)

                val mesh = boxMesh(
                    settings.boxSize.x,
                    settings.boxSize.y,
                    settings.boxSize.z,
                    settings.sides.x,
                    settings.sides.y,
                    settings.sides.z
                )

                orb.camera.rotateTo(
                   settings.r.x,
                   settings.r.y
                )

                drawer.fill = ColorRGBa.WHITE

                drawer.shadeStyle = shadeStyle {
                    fragmentTransform = """
                        x_fill.a *= v_viewNormal.z;
                    """.trimIndent()
                }
                for (i in 0 until settings.n) {
                    drawer.translate(10.0, 0.0)
                    drawer.vertexBuffer(mesh, settings.drawType)
                }

                mesh.destroy()
            }
        }
    }
}

