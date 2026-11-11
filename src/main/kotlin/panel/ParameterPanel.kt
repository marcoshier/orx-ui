package panel

import UI
import elements.titleBar
import lib.ParameterDescription
import lib.ParameterStore
import lib.XParameter
import lib.listXParameters
import org.openrndr.extra.parameters.title
import org.openrndr.shape.Rectangle
import style.Colors
import style.Style
import ui.UIElement
import ui.UIHost
import widget.Direction
import widget.Widget
import widget.WidgetBuilder
import widget.widget
import java.io.File
import kotlin.reflect.full.findAnnotation

class ParameterPanel(private val ui: UI, val style: Style? = null) {

    var file = File("ui-parameters/${UIHost.current.program.name}.conf")
    var bounds = Rectangle(10.0, 10.0, 260.0, 440.0)
    var autosave = true

    private class Binding(val key: String, val obj: Any, val params: List<XParameter>)

    private val bindings = mutableListOf<Binding>()
    private val keys = mutableSetOf<String>()
    private val store by lazy { ParameterStore(file) }

    var panel: Widget? = null
        private set

    private fun uniqueKey(base: String): String {
        if (keys.add(base)) return base
        var n = 2
        while (!keys.add("$base-$n")) n++
        return "$base-$n"
    }

    private fun getOrPutWidget(): Widget = panel ?: widget(bounds) {
        backgroundColor = Colors.CLICKABLE
        style?.let { widget.style = it }

        append(Direction.DOWN) {
            titleBar("PARAMETERS", bounds.copy(height = 15.0), widgetTitlebar = true) {
                this.style.backgroundColor = Colors.FOCUSED
            }
        }
    }.also {
        panel = it
        if (autosave) {
            ui.setupAutoSave(::saveAll)
        }
    }

    fun <T : Any> add(obj: T, key: String? = null): T {
        val params = obj.listXParameters()
        if (params.isEmpty()) return obj

        val sectionKey = uniqueKey(key ?: obj.title() ?: obj::class.simpleName ?: "parameters")
        store.load(sectionKey, obj, params)
        bindings += Binding(sectionKey, obj, params)

        val wb = WidgetBuilder(bounds, Int.MAX_VALUE)

        wb.widget = getOrPutWidget()

        wb.run {
            for (p in params) {
                elementHeight = 70.0
                append(Direction.DOWN, spacing = 10.0) {
                    val e = controlFor(p, obj)
                    val entry = p.property?.name ?: p.function?.name ?: p.label
                    (e as? UIElement)?.label = "$sectionKey.$entry"
                    (e as? UIElement)?.description = p.property?.findAnnotation<ParameterDescription>()?.text
                }
            }
        }


        UIHost.current.tree.invalidateCache()
        return obj
    }

    fun saveAll(): File {
        for (b in bindings) {
            store.put(b.key, b.obj, b.params)
        }

        store.write()
        return file
    }
}