package panel

import elements.button
import elements.checkbox
import elements.dropdown
import elements.rangeSlider
import elements.rotateControl
import elements.scaleControl
import elements.slider
import elements.textInput
import elements.textboxInput
import elements.xyControl
import lib.XParameter
import lib.XParameterType
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
import org.openrndr.shape.ShapeContour
import widget.WidgetBuilder
import widgets.BoxSizing
import widgets.contourWidget
import widgets.transformWidget
import widgets.vectorWidget
import kotlin.reflect.KMutableProperty1

@Suppress("UNCHECKED_CAST")
fun WidgetBuilder.controlFor(p: XParameter, obj: Any): Any {
    val prec = p.precision ?: 1

    return when (p.parameterType) {
        XParameterType.Double ->
            slider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>) {
                label = p.label
                precision = prec
            }

        XParameterType.Int ->
            slider(p.intRange!!, obj, p.property as KMutableProperty1<Any, Int>) {
                label = p.label
                precision = prec
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
            val prop = p.property!! as KMutableProperty1<Any, SelectableList<Any>>
            dropdown(obj, prop) { label = p.label }
        }

        XParameterType.MutableList -> {
            val prop = p.property!! as KMutableProperty1<Any, SelectableMutableList<Any>>
            dropdown(obj, prop, entryName = { e ->
                if (e is List<*>) "${p.label}-${prop.get(obj).indexOf(e)}" else e.toString()
            }) { label = p.label }
        }

        XParameterType.XY -> {
            elementHeight = elementWidth
            xyControl(obj, p.property as KMutableProperty1<Any, Vector2>, p.vectorRange!!.range) {
                precision = prec
            }
        }

        XParameterType.Scale -> {
            elementHeight = elementWidth
            val vrange = p.vectorRange?.range
            if (vrange != null) {
                scaleControl(vrange, obj, p.property as KMutableProperty1<Any, Vector2>) {
                    precision = prec
                }
            } else {
                scaleControl(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Double>) {
                    precision = prec
                }
            }
        }

        XParameterType.Rotation -> {
            elementHeight = elementWidth
            rotateControl(obj, p.property as KMutableProperty1<Any, Double>) {
                precision = prec
            }
        }

        XParameterType.Vector2 -> {
            elementHeight = 50.0 * 2
            vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector2>) {
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.Vector3 -> {
            elementHeight = 50.0 * 3
            vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector3>) {
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.Vector4 -> {
            elementHeight = 50.0 * 4
            vectorWidget(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, Vector4>) {
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.IntVector2 -> {
            elementHeight = 50.0 * 2
            vectorWidget(p.intRange!!, obj, p.property as KMutableProperty1<Any, IntVector2>) {
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.IntVector3 -> {
            elementHeight = 50.0 * 3
            vectorWidget(p.intRange!!, obj, p.property as KMutableProperty1<Any, IntVector3>) {
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.IntVector4 -> {
            elementHeight = 50.0 * 4
            vectorWidget(p.intRange!!, obj, p.property as KMutableProperty1<Any, IntVector4>) {
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.Matrix44 -> {
            elementHeight = elementWidth + 50.0
            transformWidget(
                obj, p.property as KMutableProperty1<Any, Matrix44>,
                p.doubleRange ?: -100.0..100.0,
                0.01..10.0 // TODO rotation
            ) {
                boxSizing = BoxSizing.CONTENT_BOX
                elements.forEach { it.precision = prec }
            }
        }

        XParameterType.IntRange ->
            rangeSlider(p.intRange!!, obj, p.property as KMutableProperty1<Any, ClosedRange<Int>>) {
                precision = prec
            }

        XParameterType.DoubleRange ->
            rangeSlider(p.doubleRange!!, obj, p.property as KMutableProperty1<Any, ClosedRange<Double>>) {
                precision = prec
            }

        XParameterType.Textbox ->
            textboxInput(obj, p.property as KMutableProperty1<Any, String>) {
                label = p.label
            }

        XParameterType.Contour ->
            contourWidget(obj, p.property as KMutableProperty1<Any, ShapeContour>) {
                label = p.label
            }

        XParameterType.Color -> TODO()
        XParameterType.Path -> TODO()
    }
}