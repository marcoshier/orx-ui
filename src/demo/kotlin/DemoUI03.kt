import org.openrndr.application
import org.openrndr.extra.parameters.Vector2Parameter
import org.openrndr.extra.parameters.Vector3Parameter
import org.openrndr.extra.parameters.Vector4Parameter
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4

fun main() {
    application {
        program {

            val settings = object {
                @Vector2Parameter("v2", -2.0, 2.0)
                var v2 = Vector2.ZERO

                @Vector3Parameter("v3", -2.0, 2.0)
                var v3 = Vector3.ZERO

                @Vector4Parameter("v4", -2.0, 2.0)
                var v4 = Vector4.ZERO
            }

            extend(UI()).addParameters(settings)


        }
    }
}

