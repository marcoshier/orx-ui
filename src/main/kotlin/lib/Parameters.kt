package lib

import UI
import elements.button
import elements.checkbox
import elements.dropdown
import elements.slider
import elements.textInput
import elements.titleBar
import elements.xyControl
import org.openrndr.extra.parameters.ParameterType
import org.openrndr.extra.parameters.listParameters
import org.openrndr.math.EuclideanVector
import org.openrndr.math.LinearType
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.panel.collections.SelectableList
import org.openrndr.panel.collections.SelectableMutableList
import org.openrndr.shape.Rectangle
import style.Colors
import widget.Direction
import widget.widget
import widgets.vectorWidget
import kotlin.reflect.KMutableProperty1

@Suppress("UNCHECKED_CAST")
fun <T: Any> UI.fromParameters(objectWithParameters: T): UI {
    val obj = objectWithParameters
    val parameters = objectWithParameters.listParameters()

    widget(Rectangle(10.0, 10.0, 260.0, 440.0)) {
        strokeColor = null
        backgroundColor = Colors.CLICKABLE
        padding = Vector2(10.0)

        append(Direction.DOWN) {
            titleBar("PARAMETERS", bounds.copy(height = 15.0), widgetTitlebar = true) {
                style.backgroundColor = Colors.FOCUSED
            }
        }

        for (p in parameters) {
            elementHeight = 70.0

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

                    ParameterType.List, ParameterType.DoubleList -> {
                        @Suppress("UNCHECKED_CAST")
                        val prop = p.property!! as KMutableProperty1<Any, Any>
                        val selectable = prop.get(obj) as? SelectableList<Any>
                            ?: error("You can only use lists of SelectableList type")
                        val options = selectable.toList()

                        dropdown(
                            getter = { (prop.get(obj) as SelectableList<Any>).selected },
                            setter = { s ->
                                val sl = prop.get(obj) as SelectableList<Any>
                                sl.selectedIndex = options.indexOf(s).coerceAtLeast(0)
                            },
                            entries = { options },
                            entryName = { it.toString() }
                        ) {
                            label = p.label
                        }
                    }

                    ParameterType.MutableList -> {
                        @Suppress("UNCHECKED_CAST")
                        val prop = p.property!! as KMutableProperty1<Any, Any>
                        val selectable = prop.get(obj) as? SelectableMutableList<Any>
                            ?: error("You can only use lists of SelectableMutableList type")
                        val options = selectable.toList()

                        val d = dropdown(
                            getter = { (prop.get(obj) as SelectableMutableList<Any>).selected },
                            setter = { s ->
                                val sl = prop.get(obj) as SelectableMutableList<Any>
                                sl.selectedIndex = options.indexOf(s).coerceAtLeast(0)
                            },
                            entries = { options },
                            entryName = { it.toString() }
                        ) {
                            label = p.label
                        }

                        selectable.changed.listen {
                            val l = it.toList()
                            if (l.isNotEmpty() && it.selectedIndex == -1) {
                                it.selectedIndex = 0
                            }
                            d.entries = { l }
                        }

                        d
                    }

                    ParameterType.XY -> {
                        elementHeight = elementWidth
                        val range = p.vectorRange!!.range
                        xyControl(obj, p.property as KMutableProperty1<Any, Vector2>, range)
                    }

                    ParameterType.Vector2 -> {
                        elementHeight = 50.0 * 2
                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector2>)
                    }
                    ParameterType.Vector3 -> {
                        elementHeight = 50.0 * 3
                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector3>)
                    }
                    ParameterType.Vector4 -> {
                        elementHeight = 50.0 * 4
                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector4>)
                    }

                    ParameterType.Color -> TODO()
                    ParameterType.Path -> TODO()
                }


            }
        }
    }

    return this
}