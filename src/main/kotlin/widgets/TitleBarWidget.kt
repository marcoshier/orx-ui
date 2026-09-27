package widgets

import elements.TitleBar
import org.openrndr.shape.Rectangle
import registerWidget
import ui.UIElement

class TitleBarWidget(
    label: String = "",
    bounds: Rectangle,
    zIndex: Int,
    windowTitlebar: Boolean = false,
    widgetTitlebar: Boolean = false,
): WidgetImpl(label, bounds, zIndex) {

    val titleBar = TitleBar(label, bounds, windowTitlebar, widgetTitlebar)

    override val elements: MutableList<UIElement> = mutableListOf(titleBar)
}

fun titleBarWidget(
    label: String = "",
    bounds: Rectangle,
    windowTitlebar: Boolean = false,
    widgetTitlebar: Boolean = false,
    zIndex: Int = 0,
    configure: WidgetBuilder.() -> Unit
): TitleBarWidget {
    val b = WidgetBuilder(label, bounds, zIndex)
    val tw = TitleBarWidget(label, bounds, zIndex, windowTitlebar, widgetTitlebar)
    val w = b.widget
    b.widget = tw
    w.close()
    registerWidget(b.widget)
    b.configure()
    tw.titleBar.dragMasks = tw.elements.drop(1).map { it.bounds }
    return tw
}