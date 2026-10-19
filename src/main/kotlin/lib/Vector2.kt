package lib

import org.openrndr.extra.math.linearrange.LinearRange1D
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.math.Vector2

val Pair<Vector2, Vector2>.range: LinearRange1D<Vector2> get() = first.rangeTo(second)