import ui.UIElement
import widgets.Widget

object UIRegistry {
    var tree: UITree? = null
        internal set

    private val activeTree: UITree
        get() = tree ?: error("UI has not been extended yet")

    fun addWidget(widget: Widget) = activeTree.addWidget(widget)
    fun addElement(element: UIElement) = activeTree.addElement(element)

    fun removeWidget(widget: Widget) = activeTree.removeWidget(widget)
    fun removeElement(element: UIElement) = activeTree.removeElement(element)
}

fun registerWidget(widget: Widget) { UIRegistry.addWidget(widget) }
fun registerElement(element: UIElement) { UIRegistry.addElement(element) }

fun deregisterWidget(widget: Widget) { UIRegistry.removeWidget(widget) }
fun deregisterElement(element: UIElement) { UIRegistry.removeElement(element) }