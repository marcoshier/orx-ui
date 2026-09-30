package style

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.FontMap

data class UIElementStyle(
    var idleColor: ColorRGBa = Colors.CLICKABLE,
    var hoverColor: ColorRGBa = Colors.HOVERED,
    var focusColor: ColorRGBa = Colors.FOCUSED,
    var selectColor: ColorRGBa = Colors.SELECTED,
    var stroke: ColorRGBa? = null,
    var font: FontMap = Fonts.DEFAULT,
    var textColor: ColorRGBa = Colors.BLACK,
    var rounded: Boolean = true,
    var borderRadius: Double = 2.0
)