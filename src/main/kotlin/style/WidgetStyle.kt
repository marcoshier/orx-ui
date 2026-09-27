package style

import org.openrndr.color.ColorRGBa

data class WidgetStyle(
    var paddingX: Double = 0.0,
    var paddingY: Double = 0.0,
    var minElementWidth: Double = 5.0,
    var maxElementWidth: Double = 10.0,
    var minElementHeight: Double = 5.0,
    var maxElementHeight: Double = 10.0,
    var background: ColorRGBa? = null,
    var stroke: ColorRGBa? = null
)