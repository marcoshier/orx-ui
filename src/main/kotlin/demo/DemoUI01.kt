package demo

import UI
import lib.Matrix44Parameter
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DrawPrimitive
import org.openrndr.draw.VertexFormat
import org.openrndr.extra.camera.OrbitalManual
import org.openrndr.extra.camera.ProjectionType
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.OptionParameter
import org.openrndr.extra.parameters.Vector3Parameter
import org.openrndr.math.Matrix44
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

                // TODO IntVector annotations

                @IntParameter("nx", 1, 64)
                var nx = 1

                @IntParameter("ny", 1, 64)
                var ny = 1

                @IntParameter("nz", 1, 64)
                var nz = 1

                @DoubleParameter("rx", 0.0, 360.0)
                var rx = 0.0

                @DoubleParameter("rx", 0.0, 360.0)
                var ry = 0.0

                @OptionParameter("draw type")
                val option = DrawPrimitive.TRIANGLES

            }

            GUI()

            val ui = extend(UI()).fromParameters(settings)
            extend(orb)
            extend {
                var mesh = boxMesh(
                    settings.boxSize.x,
                    settings.boxSize.y,
                    settings.boxSize.z,
                    settings.nx,
                    settings.ny,
                    settings.nz
                )

                orb.camera.rotateTo(
                   settings.rx,
                   settings.ry
                )

                drawer.fill = ColorRGBa.WHITE

                drawer.vertexBuffer(mesh, settings.option)

                mesh.destroy()
            }
        }
    }
}

