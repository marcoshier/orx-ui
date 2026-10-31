package lib

import kotlinx.serialization.Serializable
import org.openrndr.shape.Segment2D
import org.openrndr.shape.ShapeContour

@Serializable
data class SContour(
    val segments: List<Segment2D>,
    var closed: Boolean
) {
    companion object {
        fun fromContour(contour: ShapeContour): SContour {
            return SContour(
                contour.segments,
                contour.closed
            )
        }
    }
}

fun SContour.toContour() = ShapeContour.fromSegments(segments, closed)