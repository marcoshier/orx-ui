import ui.UIElement
import widget.Widget

object UIRegistry {
    var tree: UITree? = null
        internal set

    private val activeTree: UITree
        get() = tree ?: error("UI has not been extended yet")

    fun <T: Widget> addWidget(widget: T) = activeTree.addWidget(widget)
    fun <T: UIElement> addElement(element: T) = activeTree.addElement(element)

    fun removeWidget(widget: Widget) = activeTree.removeWidget(widget)
    fun removeElement(element: UIElement) = activeTree.removeElement(element)
}

fun <T: Widget> registerWidget(widget: T): T = UIRegistry.addWidget(widget)
fun  <T: Widget> registerWidgets(vararg widgets: T) = widgets.asList().map { UIRegistry.addWidget(it) }
fun deregisterWidget(widget: Widget) { UIRegistry.removeWidget(widget) }

fun <T: UIElement> registerElement(element: T): T = UIRegistry.addElement(element)
fun <T: UIElement> registerElements(vararg elements: T) = elements.asList().map { UIRegistry.addElement(it) }
fun deregisterElement(element: UIElement) { UIRegistry.removeElement(element) }
