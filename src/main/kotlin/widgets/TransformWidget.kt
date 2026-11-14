package widgets

import elements.Button
import elements.RotateControl
import elements.ScaleControl
import elements.XYControl
import lib.TransformBinding
import org.openrndr.draw.Drawer
import org.openrndr.extra.imageFit.FitMethod
import org.openrndr.extra.math.linearrange.LinearRange1D
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import ui.UIElement
import ui.registerWidget
import widget.*
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

class TransformWidget(
    bounds: Rectangle,
    val getter: () -> Matrix44,
    val setter: (Matrix44) -> Unit,
    var translateRange: LinearRange1D<Vector2>,
    var scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = { }
): Widget(bounds) {

    constructor(
        bounds: Rectangle,
        transform: KMutableProperty0<Matrix44>,
        translateRange: ClosedRange<Double>,
        scaleRange: ClosedRange<Double>,
        configure: TransformWidget.() -> Unit = { }
    ): this(bounds, { transform.get() }, { transform.set(it) },
        Vector2(translateRange.start)..Vector2(translateRange.endInclusive),
        Vector2(scaleRange.start)..Vector2(scaleRange.endInclusive),
        configure
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        bounds: Rectangle,
        obj: Any,
        transform: KMutableProperty1<Any, Matrix44>,
        translateRange: ClosedRange<Double>,
        scaleRange: ClosedRange<Double>,
        configure: TransformWidget.() -> Unit = { }
    ): this(bounds, { transform.get(obj) }, { transform.set(obj, it) },
        Vector2(translateRange.start)..Vector2(translateRange.endInclusive),
        Vector2(scaleRange.start)..Vector2(scaleRange.endInclusive),
        configure
    )

    @Suppress("UNCHECKED_CAST")
    constructor(
        bounds: Rectangle,
        obj: Any,
        transform: KMutableProperty1<Any, Matrix44>,
        translateRange: LinearRange1D<Vector2>,
        scaleRange: LinearRange1D<Vector2>,
        configure: TransformWidget.() -> Unit = { }
    ): this(bounds, { transform.get(obj) }, { transform.set(obj, it) },
        translateRange, scaleRange,
        configure
    )

    override val xScrollable = false
    override val yScrollable = false

    var precision = 1
        set(value) {
            field = value
            for (element in elements) {
                element.precision = field
            }
        }

    val ibounds = bounds.copy()
    var fitMethod = FitMethod.Cover

    private var offset = 10.0
    private var maxButtonsHeight = 50.0

    private val buttonGrid: List<Rectangle>
        get() = getCurrentButtonGrid()

    val controlBounds: Rectangle
        get() = getCurrentControlBounds()

    val buttons = asRadio(
        Button(buttonGrid[0]) { label = "translate" },
        Button(buttonGrid[1]) { label = "scale" },
        Button(buttonGrid[2]) { label = "rotate" },
    )

    private val tb = TransformBinding(getter, setter)

    private fun getCurrentButtonGrid(): List<Rectangle> {
        val r = if (fitMethod == FitMethod.Cover || fitMethod == FitMethod.Fill) {
            Rectangle(
                ibounds.x,
                ibounds.y + ibounds.width,
                ibounds.width,
                maxButtonsHeight
            ).offsetEdges(-offset, 0.0)
        } else {
            Rectangle(
                controlBounds.x,
                controlBounds.y + controlBounds.height + offset,
                controlBounds.width,
                maxButtonsHeight
            )
        }

        return r.grid(3, 1, gutterX = offset * 2, marginY = offset).flatten()
    }

    private fun getCurrentControlBounds(): Rectangle {
        return if (fitMethod == FitMethod.Cover || fitMethod == FitMethod.Fill) {
            val bi = ibounds.offsetEdges(-offset)
            bi.copy(height = bi.width)
        } else {
            val free = Rectangle(
                ibounds.x + offset,
                ibounds.y + offset,
                ibounds.width - offset * 2,
                ibounds.height - maxButtonsHeight - offset * 2
            )
            val side = minOf(free.width, free.height)
            Rectangle.fromCenter(free.center, side, side)
        }
    }

    private fun findCurrentTransformType(): String {
        return buttons.find { it.isSelected }?.label ?: "translate"
    }

    private val currentTransformType: String
        get() = findCurrentTransformType()

    private val xy = XYControl(tb::translation, translateRange, controlBounds) {
        visible = { currentTransformType == "translate" }
    }
    private val scaleControl = ScaleControl(tb::scale, scaleRange, controlBounds) {
        visible = { currentTransformType == "scale" }
    }
    private val rotateControl = RotateControl(tb::rotation, controlBounds) {
        visible = { currentTransformType == "rotate" }
    }

    init {
        addElements(listOf(xy, scaleControl, rotateControl))
        addElements(buttons)
        configure()
    }

    override fun draw(drawer: Drawer, focusElement: UIElement?) {
        bounds = if (fitMethod == FitMethod.Cover)
            listOf(ibounds, buttonGrid.bounds.offsetEdges(0.0, offset * 2)).bounds
        else ibounds

        val cb = controlBounds
        xy.bounds = cb
        scaleControl.bounds = cb
        rotateControl.bounds = cb

        super.draw(drawer, focusElement)

        for ((i, button) in buttons.withIndex()) button.bounds = buttonGrid[i]
    }
}

fun transformWidget(
    bounds: Rectangle,
    transform: KMutableProperty0<Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    zIndex: Int = 0,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = WidgetBuilder(bounds, zIndex)
    val tw = TransformWidget(bounds,
        { transform.get() },
        { transform.set(it) },
        translateRange, scaleRange,
        configure
    )
    val w = b.widget
    b.widget = tw
    w.close()
    registerWidget(b.widget)
    return tw
}

fun transformWidget(
    bounds: Rectangle,
    transform: KMutableProperty0<Matrix44>,
    translateRange: ClosedRange<Double>,
    scaleRange: ClosedRange<Double>,
    configure: TransformWidget.() -> Unit = {},
    zIndex: Int = 0,
): TransformWidget {
    val b = WidgetBuilder(bounds, zIndex)
    val tw = TransformWidget(bounds,
        transform, translateRange, scaleRange, configure
    )
    val w = b.widget
    b.widget = tw
    w.close()
    registerWidget(b.widget)
    return tw
}

fun WidgetBuilder.transformWidget(
    transform: KMutableProperty0<Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = TransformWidget(currentElementBounds,
        { transform.get() },
        { transform.set(it) },
        translateRange, scaleRange,
        configure
    )
    registerWidget(b)
    return b
}

fun WidgetBuilder.transformWidget(
   transform: KMutableProperty0<Matrix44>,
   translateRange: ClosedRange<Double>,
   scaleRange: ClosedRange<Double>,
   configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = TransformWidget(currentElementBounds,
        transform, translateRange, scaleRange, configure
    )
    registerWidget(b)
    return b
}

fun WidgetBuilder.transformWidget(
    bounds: Rectangle,
    transform: KMutableProperty0<Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = TransformWidget(
        bounds,
        { transform.get() },
        { transform.set(it) },
        translateRange,
        scaleRange,
        configure
    )

    registerWidget(b)
    return b
}


fun WidgetBuilder.transformWidget(
    obj: Any,
    transform: KMutableProperty1<Any, Matrix44>,
    translateRange: ClosedRange<Double>,
    scaleRange: ClosedRange<Double>,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = TransformWidget(currentElementBounds, obj, transform, translateRange, scaleRange, configure)
    b.parent = this.widget
    this.widget.widgets.add(b)
    return b
}

fun WidgetBuilder.transformWidget(
    obj: Any,
    transform: KMutableProperty1<Any, Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = TransformWidget(currentElementBounds, obj, transform, translateRange, scaleRange, configure)
    b.parent = this.widget
    this.widget.widgets.add(b)
    return b
}