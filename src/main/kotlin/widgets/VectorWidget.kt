package widgets

import elements.Slider
import org.openrndr.math.IntVector2
import org.openrndr.math.IntVector3
import org.openrndr.math.IntVector4
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement
import widget.Widget
import widget.WidgetBuilder
import widget.addElements
import widget.gridIndexed
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

// I don't like using Any but IntVector and Vector don't share any interface (weird)

class VectorWidget<V : Any, C : Comparable<C>>(
    bounds: Rectangle,
    var range: ClosedRange<C>,
    private val getter: () -> V,
    private val setter: (V) -> Unit,
    rowHeight: Double? = null,
    configure: VectorWidget<V, C>.() -> Unit = {}
): Widget(bounds) {

    @Suppress("UNCHECKED_CAST")
    private val functions = when(getter()) {
        is Vector2 -> listOf(
            { v: V -> (v as Vector2).x as C } to { v: V, n: C -> (v as Vector2).copy(x = n as Double) as V },
            { v: V -> (v as Vector2).y as C } to { v: V, n: C -> (v as Vector2).copy(y = n as Double) as V }
        )
        is Vector3 -> listOf(
            { v: V -> (v as Vector3).x as C } to { v: V, n: C -> (v as Vector3).copy(x = n as Double) as V },
            { v: V -> (v as Vector3).y as C } to { v: V, n: C -> (v as Vector3).copy(y = n as Double) as V },
            { v: V -> (v as Vector3).z as C } to { v: V, n: C -> (v as Vector3).copy(z = n as Double) as V }
        )
        is Vector4 -> listOf(
            { v: V -> (v as Vector4).x as C } to { v: V, n: C -> (v as Vector4).copy(x = n as Double) as V },
            { v: V -> (v as Vector4).y as C } to { v: V, n: C -> (v as Vector4).copy(y = n as Double) as V },
            { v: V -> (v as Vector4).z as C } to { v: V, n: C -> (v as Vector4).copy(z = n as Double) as V },
            { v: V -> (v as Vector4).w as C } to { v: V, n: C -> (v as Vector4).copy(w = n as Double) as V }
        )
        is IntVector2 -> listOf(
            { v: V -> (v as IntVector2).x as C } to { v: V, n: C -> (v as IntVector2).copy(x = n as Int) as V },
            { v: V -> (v as IntVector2).y as C } to { v: V, n: C -> (v as IntVector2).copy(y = n as Int) as V }
        )
        is IntVector3 -> listOf(
            { v: V -> (v as IntVector3).x as C } to { v: V, n: C -> (v as IntVector3).copy(x = n as Int) as V },
            { v: V -> (v as IntVector3).y as C } to { v: V, n: C -> (v as IntVector3).copy(y = n as Int) as V },
            { v: V -> (v as IntVector3).z as C } to { v: V, n: C -> (v as IntVector3).copy(z = n as Int) as V }
        )
        is IntVector4 -> listOf(
            { v: V -> (v as IntVector4).x as C } to { v: V, n: C -> (v as IntVector4).copy(x = n as Int) as V },
            { v: V -> (v as IntVector4).y as C } to { v: V, n: C -> (v as IntVector4).copy(y = n as Int) as V },
            { v: V -> (v as IntVector4).z as C } to { v: V, n: C -> (v as IntVector4).copy(z = n as Int) as V },
            { v: V -> (v as IntVector4).w as C } to { v: V, n: C -> (v as IntVector4).copy(w = n as Int) as V }
        )
        else -> error("Unsupported vector type")
    }

    override val elements: MutableList<UIElement> = mutableListOf()

    init {
        Vector2
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

        addElements(sliders)
        configure()
    }
}

fun <V : Any, C : Comparable<C>> vectorWidget(
    bounds: Rectangle,
    range: ClosedRange<C>,
    valueRef: KMutableProperty0<V>,
    rowHeight: Double? = null,
    configure: VectorWidget<V, C>.() -> Unit = {}
): VectorWidget<V, C> {
    val w = VectorWidget(bounds, range, { valueRef.get() }, { valueRef.set(it) }, rowHeight, configure)
    registerWidget(w)
    return w
}

fun <V : Any, C : Comparable<C>> WidgetBuilder.vectorWidget(
    bounds: Rectangle,
    range: ClosedRange<C>,
    valueRef: KMutableProperty0<V>,
    rowHeight: Double? = null,
    configure: VectorWidget<V, C>.() -> Unit = {}
): VectorWidget<V, C> {
    val w = VectorWidget(bounds, range, { valueRef.get() }, { valueRef.set(it) }, rowHeight, configure)
    w.parent = this.widget
    this.widget.widgets.add(w)
    return w
}

fun <V : Any, C : Comparable<C>> WidgetBuilder.vectorWidget(
    range: ClosedRange<C>,
    valueRef: KMutableProperty0<V>,
    rowHeight: Double? = null,
    configure: VectorWidget<V, C>.() -> Unit = {},
): VectorWidget<V, C> {
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

fun <V : Any, C : Comparable<C>> WidgetBuilder.vectorWidget(
    range: ClosedRange<C>,
    obj: Any,
    valueRef: KMutableProperty1<Any, V>,
    rowHeight: Double? = null,
    configure: VectorWidget<V, C>.() -> Unit = {},
): VectorWidget<V, C> {
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