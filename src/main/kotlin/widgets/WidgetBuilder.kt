package widgets

import org.openrndr.shape.Rectangle
import registerWidget

fun widget(
    bounds: Rectangle,
    label: String = "",
    zIndex: Int = 0,
    build: WidgetImpl.() -> Unit = {},
): WidgetImpl {
    val w = WidgetImpl(label, bounds, zIndex)
    w.build()
    registerWidget(w)
    return w
}
