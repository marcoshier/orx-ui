package lib

import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.buildTransform
import kotlin.math.atan2
import kotlin.math.sqrt

data class TRS(val translation: Vector2, val scale: Vector2, val rotation: Double)

fun Matrix44.decompose2D(): TRS {
    val sx = sqrt(c0r0 * c0r0 + c0r1 * c0r1)
    val sy = sqrt(c1r0 * c1r0 + c1r1 * c1r1)
    val rotation = if (sx == 0.0) 0.0 else Math.toDegrees(atan2(c0r1, c0r0))
    return TRS(Vector2(c3r0, c3r1), Vector2(sx, sy), rotation)
}

fun TRS.compose(): Matrix44 = buildTransform {
    translate(translation)
    rotate(rotation)
    scale(scale.x, scale.y)
}

class TransformBinding(
    val getMatrix: () -> Matrix44,
    val setMatrix: (Matrix44) -> Unit,
) {
    var translation: Vector2
        get() = getMatrix().decompose2D().translation
        set(v) { setMatrix(getMatrix().decompose2D().copy(translation = v).compose()) }

    var scale: Vector2
        get() = getMatrix().decompose2D().scale
        set(v) { setMatrix(getMatrix().decompose2D().copy(scale = v).compose()) }

    var rotation: Double
        get() = getMatrix().decompose2D().rotation
        set(v) { setMatrix(getMatrix().decompose2D().copy(rotation = v).compose()) }
}