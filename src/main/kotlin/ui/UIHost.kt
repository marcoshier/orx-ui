package ui

import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import style.StyleStack
import widget.Widget

class UIHost(val program: Program) {
    val tree = UITree()
    val style = StyleStack()
    val fonts: MutableMap<String, FontImageMap> = mutableMapOf()

    val drawer: Drawer get() = program.drawer
    val mouse get() = program.mouse
    val window get() = program.window

    private var uniqueId = 0
    fun nextUniqueId() = uniqueId++

    companion object {
        private var _current: UIHost? = null

        var current: UIHost
            get() = _current ?: error("UI has not been extended yet")
            internal set(value) { _current = value }
    }
}

fun <T : Widget> registerWidget(widget: T): T = widget.also { UIHost.current.tree.addWidget(it) }
fun <T : Widget> registerWidgets(vararg widgets: T): List<T> = widgets.map { registerWidget(it) }
fun deregisterWidget(widget: Widget) = UIHost.current.tree.removeWidget(widget)

fun <T : UIElement> registerElement(element: T): T = element.also { UIHost.current.tree.addElement(it) }
fun <T : UIElement> registerElements(vararg elements: T): List<T> = elements.map { registerElement(it) }
fun deregisterElement(element: UIElement) = UIHost.current.tree.removeElement(element)

fun mousePosition() = UIHost.current.mouse.position
fun window() = UIHost.current.window
fun seconds() = UIHost.current.program.seconds
fun clipboard() = UIHost.current.program.clipboard