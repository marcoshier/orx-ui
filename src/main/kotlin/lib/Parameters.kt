package lib

import UI
import elements.button
import elements.checkbox
import elements.dropdown
import elements.slider
import elements.textInput
import elements.titleBar
import org.openrndr.extra.parameters.ParameterType
import org.openrndr.extra.parameters.listParameters
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import style.Colors
import widgets.Direction
import widgets.widget
import kotlin.reflect.KMutableProperty1

@Suppress("UNCHECKED_CAST")
fun <T: Any> UI.fromParameters(objectWithParameters: T): UI {
    val obj = objectWithParameters
    val parameters = objectWithParameters.listParameters()

    widget(Rectangle(10.0, 10.0, 260.0, 440.0)) {
        strokeColor = null
        backgroundColor = Colors.CLICKABLE
        elementHeight = 70.0
        padding = Vector2(10.0)

        append(Direction.DOWN) {
            titleBar("PARAMETERS", bounds.copy(height = 15.0), widgetTitlebar = true) {
                style.backgroundColor = Colors.FOCUSED
            }
        }

        for (p in parameters) {
            append(Direction.DOWN, spacing = 10.0) {
                val e = when (p.parameterType) {
                    ParameterType.Double ->
                        slider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>) {
                            label = p.label
                        }

                    ParameterType.Int ->
                        slider(p.intRange!!, obj, p.property as KMutableProperty1<Any, Int>) {
                            label = p.label
                        }

                    ParameterType.Boolean ->
                        checkbox(obj, p.property as KMutableProperty1<Any, Boolean>).apply {
                            bounds = bounds.copy(width = 50.0, height = 50.0) // TODO
                        }

                    ParameterType.Action ->
                        button {
                            label = p.label
                            action = { p.function!!.call(obj) }
                        }

                    ParameterType.Text ->
                        textInput(obj, p.property as KMutableProperty1<Any, String>) {
                            label = p.label
                        }

                    ParameterType.Option ->
                        dropdown(obj, p.property as KMutableProperty1<Any, Enum<*>>)

                    else -> {
                        error("")
                    }
                }

                e.buttonUp.listen {

                }
            }
        }
    }

    return this
}