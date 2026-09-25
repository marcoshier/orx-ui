import ui.UIElement
import widgets.Widget

class UITree {

    private val widgetRoots = mutableSetOf<Widget>()
    private val elementRoots = mutableSetOf<UIElement>()

    val widgets: List<Widget> get() = widgetRoots.toList()
    val elements: List<UIElement> get() = elementRoots.toList()

    fun addWidget(widget: Widget) {
        if (widgetRoots.add(widget)) invalidateCache()
    }

    fun addElement(element: UIElement) {
        if (elementRoots.add(element)) invalidateCache()
    }

    fun removeWidget(widget: Widget) {
        if (widgetRoots.remove(widget)) invalidateCache()
    }

    fun removeElement(element: UIElement) {
        if (elementRoots.remove(element)) invalidateCache()
    }

    fun invalidateCache() {
        cache = null
    }

    private var cache: List<UIElement>? = null

    val flattened: List<UIElement>
        get() {
            cache?.let { return it }
            val out = mutableListOf<UIElement>()
            for (widget in widgetRoots) {
                for (element in widget.elements) walk(element, out)
            }
            for (element in elementRoots) walk(element, out)
            out.sortBy { it.zIndex }
            cache = out
            return out
        }

    val drawOrder: List<UIElement> get() = flattened
    val hitOrder: List<UIElement>
        get() {
            val reversed = flattened.asReversed()
            return reversed.filter { it.isFocused } + reversed.filterNot { it.isFocused }
        }

    private fun walk(element: UIElement, out: MutableList<UIElement>) {
        out.add(element)
        if (element is Root) {
            for (child in element.children) {
                walk(child, out)
            }
        }
    }
}

interface Root {
    val children: List<UIElement>
}
