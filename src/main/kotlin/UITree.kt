import org.openrndr.math.Vector2
import ui.UIElement
import widgets.Widget
import widgets.screenBounds

class UITree {

    private val widgetRoots = mutableSetOf<Widget>()
    private val elementRoots = mutableSetOf<UIElement>()

    val widgets: List<Widget> get() = widgetRoots.toList()
    val elements: List<UIElement> get() = elementRoots.toList()

    fun addWidget(widget: Widget): Widget {
        if (widgetRoots.add(widget)) invalidateCache()
        return widget
    }

    fun addElement(element: UIElement): UIElement {
        if (elementRoots.add(element)) invalidateCache()
        return element
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
                traverseWidget(widget, out)
            }
            for (element in elementRoots) {
                traverseElement(element, out)
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
}

private fun traverseElement(element: UIElement, out: MutableList<UIElement>) {
    out.add(element)
    if (element is Root) {
        for (child in element.children) {
            traverseElement(child, out)
        }
    }
}

private fun traverseWidget(widget: Widget, out: MutableList<UIElement>) {
    for (el in widget.elements) {
        traverseElement(el, out)
    }
    for (child in widget.widgets) {
        traverseWidget(child, out)
    }
}

fun UIElement.walk(): MutableList<UIElement> {
    val out = mutableListOf<UIElement>()
    traverseElement(this, out)
    return out
}

fun UIElement.ancestors(): MutableList<Widget> {
    val out = mutableListOf<Widget>()
    var current = this.parent

    while (current != null) {
        out.add(current)
        current = current.parent
    }

    return out
}

fun Widget.walk(): MutableList<UIElement> {
    val out = mutableListOf<UIElement>()
    traverseWidget(this, out)
    return out
}

fun Widget.ancestors(): MutableList<Widget> {
    val out = mutableListOf<Widget>()
    var current = this.parent

    while (current != null) {
        out.add(current)
        current = current.parent
    }

    return out
}

fun leafWidget(pos: Vector2, widgets: List<Widget> = UIHost.current.tree.widgets): Widget? {
    for (w in widgets.sortedByDescending { it.zIndex }) {
        val screenBounds = w.screenBounds()
        if (pos in screenBounds) {
            val next = leafWidget(pos, w.widgets)
            if (next != null) return next
            if (w.yScrollable || w.xScrollable) return w
        }
    }
    return null
}

interface Root {
    val children: List<UIElement>
}

