package style

import kotlinx.serialization.Serializable
import org.openrndr.animatable.easing.Easing
import org.openrndr.color.ColorRGBa

@Serializable
data class TransitionDescriptor(
    val duration: Double = 0.3,
    val ease: Easing = Easing.None,
    val delay: Double = 0.0,
)

class DoubleTransition(val from: Double, val to: Double, val start: Double, val descriptor: TransitionDescriptor?) {

    fun valueAt(t: Double): Double {
        val s = descriptor ?: return to
        if (s.duration <= 0.0) return to

        val ct = ((t - start - s.delay) / s.duration).coerceIn(0.0, 1.0)
        return from + (to - from) * s.ease.easer.ease(ct, 0.0, 1.0, 1.0)
    }

    fun completed(t: Double) = descriptor == null || t >= (start + descriptor.delay + descriptor.duration)
}

class ColorRGBaTransition(val from: ColorRGBa?, val to: ColorRGBa?, val start: Double, val descriptor: TransitionDescriptor?) {

    fun valueAt(t: Double): ColorRGBa? {
        val s = descriptor ?: return to
        if (s.duration <= 0.0) return to

        val ct = ((t - start - s.delay) / s.duration).coerceIn(0.0, 1.0)
        return if (from != null && to != null) from.mix(to, s.ease.easer.ease(ct, 0.0, 1.0, 1.0)) else null
    }

    fun completed(t: Double) = descriptor == null || t >= (start + descriptor.delay + descriptor.duration)
}

