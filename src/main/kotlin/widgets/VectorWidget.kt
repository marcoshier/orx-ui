package widgets

import elements.Slider
import org.openrndr.math.EuclideanVector
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement
import widget.Widget
import widget.WidgetBuilder
import widget.gridIndexed
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class VectorWidget<T: EuclideanVector<T>>(
    bounds: Rectangle,
    var range: ClosedRange<Double>,
    private val getter: () -> T,
    private val setter: (T) -> Unit,
    rowHeight: Double? = null,
    configure: VectorWidget<T>.() -> Unit = {}
): Widget(bounds) {

    @Suppress("UNCHECKED_CAST")
    private val functions = when(getter()) {
        is Vector2 -> listOf(
            { v: T -> (v as Vector2).x } to { v: T, n: Double -> (v as Vector2).copy(x = n) as T },
            { v: T -> (v as Vector2).y } to { v: T, n: Double -> (v as Vector2).copy(y = n) as T }
        )
        is Vector3 -> listOf(
            { v: T -> (v as Vector3).x } to { v: T, n: Double -> (v as Vector3).copy(x = n) as T },
            { v: T -> (v as Vector3).y } to { v: T, n: Double -> (v as Vector3).copy(y = n) as T },
            { v: T -> (v as Vector3).z } to { v: T, n: Double -> (v as Vector3).copy(z = n) as T }
        )
        is Vector4 -> listOf(
            { v: T -> (v as Vector4).x } to { v: T, n: Double -> (v as Vector4).copy(x = n) as T },
            { v: T -> (v as Vector4).y } to { v: T, n: Double -> (v as Vector4).copy(y = n) as T },
            { v: T -> (v as Vector4).z } to { v: T, n: Double -> (v as Vector4).copy(z = n) as T },
            { v: T -> (v as Vector4).w } to { v: T, n: Double -> (v as Vector4).copy(w = n) as T }
        )
        else -> error("Unsupported vector type")
    }

    override val elements: MutableList<UIElement> = mutableListOf()

    init {
        val sliderFunc: (Int, Rectangle) -> UIElement = { i, r ->
            val (read, write) = functions[i]
            val axes = "xyzw"

            Slider(r, range, { read(getter()) }, { setter(write(getter(), it)) }) {
                label = "${axes[i]}-${this@VectorWidget.label}"
            }
        }

        val sliders = if (rowHeight != null) {
            gridIndexed(bounds.width, rowHeight, gutterY = 3.0) { i, r -> sliderFunc(i, r) }
        } else {
            gridIndexed(1, functions.size, marginY = 3.0, gutterY = 3.0) { i, r -> sliderFunc(i, r) }
        }

        elements.addAll(sliders)
        configure()
    }
}

fun <T : EuclideanVector<T>> vectorWidget(
    bounds: Rectangle,
    range: ClosedRange<Double>,
    valueRef: KMutableProperty0<T>,
    rowHeight: Double? = null,
    configure: VectorWidget<T>.() -> Unit = {}
): VectorWidget<T> {
    val w = VectorWidget(bounds, range, { valueRef.get() }, { valueRef.set(it) }, rowHeight, configure)
    registerWidget(w)
    return w
}

fun <T : EuclideanVector<T>> WidgetBuilder.vectorWidget(
    bounds: Rectangle,
    range: ClosedRange<Double>,
    valueRef: KMutableProperty0<T>,
    rowHeight: Double? = null,
    configure: VectorWidget<T>.() -> Unit = {}
): VectorWidget<T> {
    val w = VectorWidget(bounds, range, { valueRef.get() }, { valueRef.set(it) }, rowHeight, configure)
    w.parent = this.widget
    this.widget.widgets.add(w)
    return w
}

fun <T : EuclideanVector<T>> WidgetBuilder.vectorWidget(
    range: ClosedRange<Double>,
    valueRef: KMutableProperty0<T>,
    rowHeight: Double? = null,
    configure: VectorWidget<T>.() -> Unit = {},
): VectorWidget<T> {
    val r = Rectangle(
        currentX,
        currentY,
        elementWidth,
        elementHeight
    )
    val w = VectorWidget(r, range, { valueRef.get() }, { valueRef.set(it) }, rowHeight, configure)
    w.parent = this.widget
    this.widget.widgets.add(w)
    return w
}

fun <T : EuclideanVector<T>> WidgetBuilder.vectorWidget(
    range: ClosedRange<Double>,
    obj: Any,
    valueRef: KMutableProperty1<Any, T>,
    rowHeight: Double? = null,
    configure: VectorWidget<T>.() -> Unit = {},
): VectorWidget<T> {
    val r = Rectangle(
        currentX,
        currentY,
        elementWidth,
        elementHeight
    )
    val w = VectorWidget(r, range, { valueRef.get(obj) }, { valueRef.set(obj, it) }, rowHeight, configure)
    w.parent = this.widget
    this.widget.widgets.add(w)
    return w
}