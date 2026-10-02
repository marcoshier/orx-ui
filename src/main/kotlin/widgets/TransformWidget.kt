package widgets

import elements.Button
import elements.RotateControl
import elements.ScaleControl
import elements.XYControl
import lib.TransformBinding
import org.openrndr.draw.Drawer
import org.openrndr.extra.imageFit.FitMethod
import org.openrndr.extra.imageFit.fitRectangle
import org.openrndr.extra.math.linearrange.LinearRange1D
import org.openrndr.extra.math.linearrange.rangeTo
import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import registerWidget
import style.Colors
import ui.UIElement
import kotlin.reflect.KMutableProperty0

enum class BoxSizing {
    CONTENT_BOX,
    BORDER_BOX,
}

class TransformWidget(
    label: String,
    bounds: Rectangle,
    val getter: () -> Matrix44,
    val setter: (Matrix44) -> Unit,
    val translateRange: LinearRange1D<Vector2>,
    val scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = { }
): WidgetImpl(label, bounds) {

    constructor(
        label: String,
        bounds: Rectangle,
        transform: KMutableProperty0<Matrix44>,
        translateRange: ClosedFloatingPointRange<Double>,
        scaleRange: ClosedFloatingPointRange<Double>,
        configure: TransformWidget.() -> Unit = { }
    ): this(label, bounds, { transform.get() }, { transform.set(it) },
        Vector2(translateRange.start)..Vector2(translateRange.endInclusive),
        Vector2(scaleRange.start)..Vector2(scaleRange.endInclusive),
        configure
    )

    val ibounds = bounds.copy()
    var boxSizing = BoxSizing.CONTENT_BOX

    private var offset = 5.0
    private var maxButtonsHeight = 50.0

    private val buttonGrid: List<Rectangle>
        get() = getCurrentButtonGrid()

    val controlBounds: Rectangle
        get() = getCurrentControlBounds()

    val buttons = asRadio(
        Button("translate", buttonGrid[0]),
        Button("scale", buttonGrid[1]),
        Button("rotate", buttonGrid[2]),
    )

    private val tb = TransformBinding(getter, setter)

    private fun getCurrentButtonGrid(): List<Rectangle> {
        val r = if (boxSizing == BoxSizing.CONTENT_BOX) {
            Rectangle(
                ibounds.x,
                ibounds.y + ibounds.width,
                ibounds.width,
                maxButtonsHeight
            ).offsetEdges(-offset, 0.0)
        } else {
            Rectangle(
                controlBounds.x,
                bounds.y + bounds.height - maxButtonsHeight,
                controlBounds.width,
                maxButtonsHeight
            )
        }

        return r.grid(3, 1, gutterX = offset * 2, marginY = offset).flatten()
    }

    private fun getCurrentControlBounds(): Rectangle {
        return if (boxSizing == BoxSizing.CONTENT_BOX) {
            val bi = ibounds.offsetEdges(-offset)
            bi
        } else {
            val bi = bounds.offsetEdges(-offset)
            val bo = Rectangle.fromCenter(
                bounds.copy(height = bounds.height - maxButtonsHeight).center,
                bounds.width, bounds.height - maxButtonsHeight
            )
            val (_,target) = fitRectangle(bi, bo, fitMethod = FitMethod.Contain)
            target.offsetEdges(-offset)
        }
    }

    private fun findCurrentTransformType(): String {
        return buttons.find { it.isSelected }?.label ?: "translate"
    }

    private val currentTransformType: String
        get() = findCurrentTransformType()

    init {
        style.background = Colors.CLICKABLE.shade(1.5)
        configure()
    }

    override val elements = (listOf<UIElement>(
        XYControl("translate", controlBounds, tb::translation, translateRange) { visibleIf = { currentTransformType == "translate" } },
        ScaleControl("scale", controlBounds, tb::scale, scaleRange) { visibleIf = { currentTransformType == "scale" } },
        RotateControl("rotate", controlBounds, tb::rotation) { visibleIf = { currentTransformType == "rotate" } }
    ) + buttons).toMutableList()

    override fun draw(drawer: Drawer) {
        bounds = if (boxSizing == BoxSizing.CONTENT_BOX) {
           listOf(ibounds, buttonGrid.bounds.offsetEdges(0.0, offset * 2)).bounds
        } else ibounds

        super.draw(drawer)

        for ((i, button) in buttons.withIndex()) {
            button.bounds = buttonGrid[i]
        }
    }
}

fun transformWidget(
    label: String = "",
    bounds: Rectangle,
    transform: KMutableProperty0<Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    zIndex: Int = 0,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = WidgetBuilder(label, bounds, zIndex)
    val tw = TransformWidget(label, bounds,
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
    label: String = "",
    bounds: Rectangle,
    transform: KMutableProperty0<Matrix44>,
    translateRange: ClosedFloatingPointRange<Double>,
    scaleRange: ClosedFloatingPointRange<Double>,
    configure: TransformWidget.() -> Unit = {},
    zIndex: Int = 0,
): TransformWidget {
    val b = WidgetBuilder(label, bounds, zIndex)
    val tw = TransformWidget(label, bounds,
        transform, translateRange, scaleRange, configure
    )
    val w = b.widget
    b.widget = tw
    w.close()
    registerWidget(b.widget)
    return tw
}

fun WidgetBuilder.transformWidget(
    label: String,
    transform: KMutableProperty0<Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val r = Rectangle(
        itemX,
        itemY,
        itemWidth,
        itemHeight
    )
    val b = TransformWidget(label, r,
        { transform.get() },
        { transform.set(it) },
        translateRange, scaleRange,
        configure
    )
    registerWidget(b)
    return b
}

fun WidgetBuilder.transformWidget(
   label: String,
   transform: KMutableProperty0<Matrix44>,
   translateRange: ClosedFloatingPointRange<Double>,
   scaleRange: ClosedFloatingPointRange<Double>,
   configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val r = Rectangle(
        itemX,
        itemY,
        itemWidth,
        itemHeight
    )
    val b = TransformWidget(label, r,
        transform, translateRange, scaleRange, configure
    )
    registerWidget(b)
    return b
}

fun WidgetBuilder.transformWidget(
    label: String,
    bounds: Rectangle,
    transform: KMutableProperty0<Matrix44>,
    translateRange: LinearRange1D<Vector2>,
    scaleRange: LinearRange1D<Vector2>,
    configure: TransformWidget.() -> Unit = {}
): TransformWidget {
    val b = TransformWidget(
        label,
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


