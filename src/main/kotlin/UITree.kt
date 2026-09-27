import org.openrndr.math.Vector2
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
                walkWidget(widget, out)
            }
            for (element in elementRoots) {
                walk(element, out)
            }

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

    private fun walkWidget(w: Widget, out: MutableList<UIElement>) {
        for (el in w.elements) {
            walk(el, out)
        }
        for (child in w.widgets) {
            walkWidget(child, out)
        }
    }

    fun leafWidget(pos: Vector2, widgets: List<Widget> = this.widgets): Widget? {
        for (w in widgets.sortedByDescending { it.zIndex }) {
            val parentOffset = w.parent?.effectiveYOffset ?: 0.0
            val screenBounds = w.bounds.movedBy(Vector2(0.0, parentOffset))
            if (pos in screenBounds) {
                val deeper = leafWidget(pos, w.widgets)
                if (deeper != null) return deeper
                if (w.yScrollable || w.xScrollable) return w
            }
        }
        return null
    }
}

interface Root {
    val children: List<UIElement>
}
