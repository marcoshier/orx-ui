package panel

import UI
import elements.titleBar
import lib.*
import org.openrndr.draw.Drawer
import org.openrndr.extra.parameters.title
import org.openrndr.extra.textwriter.writer
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

    var file = defaultParameterFile()
    var bounds = Rectangle(10.0, 10.0, 260.0, 440.0)
    var autosave = true

    private class Binding(val key: String, val obj: Any, val params: List<XParameter>)

    private val bindings = mutableListOf<Binding>()
    private val keys = mutableSetOf<String>()
    private val store by lazy { ParameterStore(file) }

    var panel: Widget? = null
        private set

    private fun defaultParameterFile(): File {
        val mainClass = Thread.currentThread().stackTrace.lastOrNull()?.className
            ?: UIHost.current.program.name
        val name = mainClass.removeSuffix("Kt").lowercase()
        return File("ui-parameters/$name.conf")
    }

    private fun uniqueKey(base: String): String {
        if (keys.add(base)) return base
        var n = 2
        while (!keys.add("$base-$n")) n++
        return "$base-$n"
    }

    private fun getOrPutWidget(): Widget = panel ?: widget(bounds) {
        backgroundColor = Colors.CLICKABLE.shade(1.2)
        paddingX = 7.0
        paddingY = 7.0
        widget.minYOffset = -9.0
        widget.yOffset = -9.0
        style?.let { widget.style = it }

        append(Direction.DOWN) {
            titleBar("PARAMETERS", bounds.copy(height = 20.0), widgetTitlebar = true) {
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
                val name = p.label.ifBlank { p.property?.name ?: p.function?.name ?: p.label }

                elementHeight = 16.0
                append(Direction.DOWN, spacing = 15.0) {
                    add(PanelLabel(name, currentElementBounds))
                }

                elementHeight = heightFor(p, elementWidth)
                append(Direction.DOWN, spacing = 5.0) {
                    when (val e = controlFor(p, obj)) {
                        is UIElement -> {
                            e.label = ""
                            e.description = p.property?.findAnnotation<ParameterDescription>()?.text
                        }
                        is Widget -> {}
                    }
                }
            }
        }


        UIHost.current.tree.invalidateCache()
        return obj
    }

    private fun heightFor(p: XParameter, width: Double): Double = when (p.parameterType) {
        XParameterType.XY, XParameterType.Scale, XParameterType.Rotation -> width
        XParameterType.Vector2, XParameterType.IntVector2 -> 50.0 * 2
        XParameterType.Vector3, XParameterType.IntVector3 -> 50.0 * 3
        XParameterType.Vector4, XParameterType.IntVector4 -> 50.0 * 4
        XParameterType.Matrix44 -> width + 50.0
        XParameterType.Contour -> width
        XParameterType.Boolean -> 50.0
        XParameterType.Textbox -> 90.0
        else -> 50.0
    }

    fun saveAll(): File {
        for (b in bindings) {
            store.put(b.key, b.obj, b.params)
        }

        store.write()
        return file
    }
}

class PanelLabel(
    var text: String,
    bounds: Rectangle? = null,
): UIElement(bounds ?: Rectangle(10.0, 10.0, 100.0, 16.0)) {
    init {
        interactable = { false }
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible()) return

        drawer.fontMap = effectiveStyle.fontMap
        drawer.fill = effectiveStyle.textColor
        drawer.stroke = null
        drawer.writer {
            box = this@PanelLabel.bounds.offsetEdges(-5.0, 0.0)
            verticalAlign = 1.0
            text(this@PanelLabel.text.uppercase())
        }
    }
}