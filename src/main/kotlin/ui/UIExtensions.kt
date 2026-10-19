package ui

import UI
import elements.button
import elements.checkbox
import elements.dropdown
import elements.rangeSlider
import elements.rotateControl
import elements.scaleControl
import elements.slider
import elements.textInput
import elements.textboxInput
import elements.titleBar
import elements.xyControl
import io.github.oshai.kotlinlogging.KotlinLogging
import lib.ParameterStore
import lib.XParameterType
import lib.listXParameters
import lib.range
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.panel.collections.SelectableList
import org.openrndr.panel.collections.SelectableMutableList
import org.openrndr.shape.Rectangle
import style.Colors
import widget.Direction
import widget.widget
import widgets.transformWidget
import widgets.vectorWidget
import java.io.File
import kotlin.reflect.KMutableProperty1

private val logger = KotlinLogging.logger {  }

@Suppress("UNCHECKED_CAST")
fun <T: Any> UI.fromParameters(
    objectWithParameters: T,
    file: File? = null,
    autosave: Boolean = true
): UI {
    val obj = objectWithParameters
    val parameters = objectWithParameters.listXParameters()

    val dest = file ?: File("ui-parameters/${UIHost.context.program.name}.conf")
    val store = ParameterStore(dest)
    val key = obj::class.simpleName ?: "parameters"
    store.load(key, obj, parameters)

    fun save(): File {
        logger.info { "saving to $file" }
        store.save(key, obj, parameters)
        return dest
    }

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
                    XParameterType.Double ->
                        slider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>) {
                            label = p.label
                        }

                    XParameterType.Int ->
                        slider(p.intRange!!, obj, p.property as KMutableProperty1<Any, Int>) {
                            label = p.label
                        }

                    XParameterType.Boolean ->
                        checkbox(obj, p.property as KMutableProperty1<Any, Boolean>).apply {
                            bounds = bounds.copy(width = 50.0, height = 50.0) // TODO
                        }

                    XParameterType.Action ->
                        button {
                            label = p.label
                            action = { p.function!!.call(obj) }
                        }

                    XParameterType.Text ->
                        textInput(obj, p.property as KMutableProperty1<Any, String>) {
                            label = p.label
                        }

                    XParameterType.Option ->
                        dropdown(obj, p.property as KMutableProperty1<Any, Enum<*>>)

                    XParameterType.List, XParameterType.DoubleList -> {
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

                    XParameterType.MutableList -> {
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

                    XParameterType.XY -> {
                        elementHeight = elementWidth
                        xyControl(obj, p.property as KMutableProperty1<Any, Vector2>, p.vectorRange!!.range)
                    }
                    XParameterType.Scale -> {
                        elementHeight = elementWidth
                        val vrange = p.vectorRange?.range
                        if (vrange != null) {
                            scaleControl(vrange, obj, p.property as KMutableProperty1<Any, Vector2>)
                        } else {
                            scaleControl(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>)
                        }
                    }
                    XParameterType.Rotation -> {
                        elementHeight = elementWidth
                        rotateControl(obj, p.property as KMutableProperty1<Any, Double>)
                    }
                    XParameterType.Vector2 -> {
                        elementHeight = 50.0 * 2
                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector2>)
                    }
                    XParameterType.Vector3 -> {
                        elementHeight = 50.0 * 3
                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector3>)
                    }
                    XParameterType.Vector4 -> {
                        elementHeight = 50.0 * 4
                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector4>)
                    }
                    XParameterType.Matrix44 -> {
                        elementHeight = elementWidth + 50.0
                        transformWidget(obj, p.property as KMutableProperty1<Any, Matrix44>,
                            p.doubleRange ?: -100.0..100.0,
                            0.01..10.0 // TODO rotation
                        )
                    }
                    XParameterType.IntRange -> {
                        rangeSlider(p.intRange!!, obj, p.property as KMutableProperty1<Any, ClosedRange<Int>>)
                    }
                    XParameterType.DoubleRange -> {
                        rangeSlider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, ClosedRange<Double>>)
                    }
                    XParameterType.Textbox ->
                        textboxInput(obj, p.property as KMutableProperty1<Any, String>) {
                            label = p.label
                        }

                    XParameterType.Contour -> TODO()
                    XParameterType.Color -> TODO()
                    XParameterType.Path -> TODO()
                }
            }
        }
    }

    if (autosave) {
        setupAutoSave(::save)
    }

    return this
}