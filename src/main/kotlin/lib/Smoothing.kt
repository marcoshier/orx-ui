package lib

import org.openrndr.extra.delegatemagic.smoothing.DoublePropertySmoother
import kotlin.math.pow
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

// custom DoublePropertySmoothing with edges
fun smoothing(property: KProperty0<Double>, factor: Double = 0.99): DoublePropertySmoother {
    return DoublePropertySmoother(UIHost.context.program, property, factor, null)
}