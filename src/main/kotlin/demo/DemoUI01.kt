package demo

import UI
import lib.fromParameters
import org.openrndr.ExtensionStage
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.defaultFontMap
import org.openrndr.extra.color.spaces.ColorOKLCHa
import org.openrndr.extra.color.tools.shiftHue
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.OptionParameter
import org.openrndr.extra.parameters.TextParameter
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle


private enum class ShapeType {
    RECTANGLE, CIRCLE
}

fun main() {
    application {
        program {
            var color = ColorRGBa.RED
            val settings = object {
                @DoubleParameter("x", 0.0, 5.0, precision = 3)
                var x = 5.0

                @IntParameter("y", 0, 10)
                var y = 0

                @DoubleParameter("size", 0.0, 10.0, precision = 3)
                var size = 5.0

                @BooleanParameter("visible")
                var visible = true

                @ActionParameter("change color")
                fun changeColor() {
                    color = color.shiftHue<ColorOKLCHa>(30.0)
                }

                @TextParameter("text")
                var text = ""

                @OptionParameter("shape type")
                var type = ShapeType.CIRCLE
            }


            extend(UI()).fromParameters(settings)

            extend {
                if (settings.visible) {
                    drawer.fill = color
                    val p = drawer.bounds.center + Vector2(20.0 * settings.x, 20.0 * settings.y)
                    val r = 20.0 * settings.size
                    when(settings.type) {
                        ShapeType.CIRCLE -> drawer.circle(p, r)
                        ShapeType.RECTANGLE -> drawer.rectangle(Rectangle.fromCenter(p, r * 2))
                    }
                }

                drawer.stroke = null
                drawer.fill = ColorRGBa.WHITE
                drawer.fontMap = defaultFontMap
                drawer.text(settings.text, drawer.bounds.center)

            }
        }
    }
}

