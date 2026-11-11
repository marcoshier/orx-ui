package lib

import org.openrndr.extra.delegatemagic.smoothing.DoublePropertySmoother
import kotlin.math.pow
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

fun smoothing(property: KProperty0<Double>, factor: Double = 0.99): DoublePropertySmoother {
    return DoublePropertySmoother(UIHost.current.program, property, factor, null)
}