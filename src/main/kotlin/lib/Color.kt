package lib

import org.openrndr.color.ColorRGBa

fun ColorRGBa?.mixOrNull(other: ColorRGBa?, x: Double): ColorRGBa? {
    if (this == null || other == null) return null
    return mix(other, x)
}