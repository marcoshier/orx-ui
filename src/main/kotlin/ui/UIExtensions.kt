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
import lib.ParameterDescription
import lib.ParameterStore
import lib.XParameterType
import lib.listXParameters
import lib.range
import org.openrndr.math.IntVector2
import org.openrndr.math.IntVector3
import org.openrndr.math.IntVector4
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.panel.collections.SelectableList
import org.openrndr.panel.collections.SelectableMutableList
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import style.Colors
import style.Style
import widget.Direction
import widget.Widget
import widget.WidgetBuilder
import widget.widget
import widgets.BoxSizing
import widgets.contourWidget
import widgets.transformWidget
import widgets.vectorWidget
import java.io.File
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation

private val logger = KotlinLogging.logger {  }
//
//@Suppress("UNCHECKED_CAST")
//fun <T: Any> UI.fromParameters(
//    objectWithParameters: T,
//    file: File? = null,
//    style: Style? = null,
//    autosave: Boolean = true,
//    configure: Widget.() -> Unit = {}
//): Widget {
//    val obj = objectWithParameters
//    val parameters = objectWithParameters.listXParameters()
//
//    val saveFile = file ?: File("ui-parameters/${ui.UIHost.context.program.name}.conf")
//    val store = ParameterStore(saveFile)
//
//    val usedParameterKeys = mutableSetOf<String>()
//
//    val key = obj::class.simpleName ?: "parameters"
//    store.load(key, obj, parameters)
//
//    fun save(): File {
//        logger.info { "saving to $saveFile" }
//        store.save(key, obj, parameters)
//        return saveFile
//    }
//
//
//    val w = widget(Rectangle(10.0, 10.0, 260.0, 440.0)) {
//        strokeColor = style?.strokeColor
//        backgroundColor = style?.backgroundColor ?: Colors.CLICKABLE
//        padding = Vector2(style?.paddingX ?: 10.0, style?.paddingY ?: 10.0)
//        style?.let { widget.style = it }
//
//        append(Direction.DOWN) {
//            titleBar("PARAMETERS", bounds.copy(height = 15.0), widgetTitlebar = true) {
//                this.style.backgroundColor = Colors.FOCUSED
//            }
//        }
//
//        for (p in parameters) {
//            elementHeight = 70.0
//
//
//            append(Direction.DOWN, spacing = 10.0) {
//               val e = when (p.parameterType) {
//                    XParameterType.Double ->
//                        slider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>) {
//                            label = p.label
//                            precision = p.precision ?: 1
//                        }
//
//                    XParameterType.Int ->
//                        slider(p.intRange!!, obj, p.property as KMutableProperty1<Any, Int>) {
//                            label = p.label
//                            precision = p.precision ?: 1
//                        }
//
//                    XParameterType.Boolean ->
//                        checkbox(obj, p.property as KMutableProperty1<Any, Boolean>).apply {
//                            bounds = bounds.copy(width = 50.0, height = 50.0) // TODO
//                        }
//
//                    XParameterType.Action ->
//                        button {
//                            label = p.label
//                            action = { p.function!!.call(obj) }
//                        }
//
//                    XParameterType.Text ->
//                        textInput(obj, p.property as KMutableProperty1<Any, String>) {
//                            label = p.label
//                        }
//
//                    XParameterType.Option ->{
//                        dropdown(obj, p.property as KMutableProperty1<Any, Enum<*>>)
//                    }
//
//                   XParameterType.List, XParameterType.DoubleList -> {
//                       @Suppress("UNCHECKED_CAST")
//                       val prop = p.property!! as KMutableProperty1<Any, SelectableList<Any>>
//                       dropdown(obj, prop) { label = p.label }
//                   }
//
//                   XParameterType.MutableList -> {
//                       @Suppress("UNCHECKED_CAST")
//                       val prop = p.property!! as KMutableProperty1<Any, SelectableMutableList<Any>>
//                       dropdown(obj, prop, entryName = { e ->
//                           if (e is List<*>) "${p.label}-${prop.get(obj).indexOf(e)}" else e.toString()
//                       }) { label = p.label }
//                   }
//
//                    XParameterType.XY -> {
//                        elementHeight = elementWidth
//                        xyControl(obj, p.property as KMutableProperty1<Any, Vector2>, p.vectorRange!!.range) {
//                            precision = p.precision ?: 1
//                        }
//                    }
//
//                    XParameterType.Scale -> {
//                        elementHeight = elementWidth
//                        val vrange = p.vectorRange?.range
//                        if (vrange != null) {
//                            scaleControl(vrange, obj, p.property as KMutableProperty1<Any, Vector2>) {
//                                precision = p.precision ?: 1
//                            }
//                        } else {
//                            scaleControl(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>) {
//                                precision = p.precision ?: 1
//                            }
//                        }
//                    }
//                    XParameterType.Rotation -> {
//                        elementHeight = elementWidth
//                        rotateControl(obj, p.property as KMutableProperty1<Any, Double>) {
//                            precision = p.precision ?: 1
//                        }
//                    }
//                    XParameterType.Vector2 -> {
//                        elementHeight = 50.0 * 2
//                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector2>) {
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.Vector3 -> {
//                        elementHeight = 50.0 * 3
//                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector3>) {
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.Vector4 -> {
//                        elementHeight = 50.0 * 4
//                        vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector4>) {
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.IntVector2 -> {
//                        elementHeight = 50.0 * 2
//                        vectorWidget(p.intRange!!, obj, p.property as KMutableProperty1<Any, IntVector2>) {
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.IntVector3 -> {
//                        elementHeight = 50.0 * 3
//                        vectorWidget(p.intRange!!, obj, p.property as KMutableProperty1<Any, IntVector3>) {
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.IntVector4 -> {
//                        elementHeight = 50.0 * 4
//                        vectorWidget(p.intRange!!, obj, p.property as KMutableProperty1<Any, IntVector4>) {
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.Matrix44 -> {
//                        elementHeight = elementWidth + 50.0
//                        transformWidget(obj, p.property as KMutableProperty1<Any, Matrix44>,
//                            p.doubleRange ?: -100.0..100.0,
//                            0.01..10.0 // TODO rotation
//                        ) {
//                            boxSizing = BoxSizing.CONTENT_BOX
//                            elements.forEach { it.precision = p.precision ?: 1 }
//                        }
//                    }
//                    XParameterType.IntRange -> {
//                        rangeSlider(p.intRange!!, obj, p.property as KMutableProperty1<Any, ClosedRange<Int>>) {
//                            precision = p.precision ?: 1
//                        }
//                    }
//                    XParameterType.DoubleRange -> {
//                        rangeSlider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, ClosedRange<Double>>) {
//                            precision = p.precision ?: 1
//                        }
//                    }
//                    XParameterType.Textbox ->
//                        textboxInput(obj, p.property as KMutableProperty1<Any, String>) {
//                            label = p.label
//                        }
//                    XParameterType.Contour -> {
//                        contourWidget(obj, p.property as KMutableProperty1<Any, ShapeContour>) {
//                            label = p.label
//                        }
//                    }
//                    XParameterType.Color -> TODO()
//                    XParameterType.Path -> TODO()
//               }
//            }
//        }
//    }
//
//    if (autosave) {
//        setupAutoSave(::save)
//    }
//
//    configure(w)
//
//    return w
//}