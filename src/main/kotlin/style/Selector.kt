package style

import UI
import UITree
import elements.button
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import ui.UIElement
import widgets.gridSlots
import widgets.widget
import java.io.File

class Font(val name: String = "default", val format: String = "otf", val path: String = "data/fonts") {
    companion object {
        val DEFAULT = Font()
    }
}

data class Style(
    // layouting
    var marginX: Double = 0.0,
    var marginY: Double = 0.0,
    var paddingX: Double = 0.0,
    var paddingY: Double = 0.0,
    var minWidth: Double = 5.0,
    var maxWidth: Double = 10.0,
    var minHeight: Double = 5.0,
    var maxHeight: Double = 10.0,

    // text
    var fontFamily: Font = Font(),
    var fontSize: Double = 16.0,
    var textColor: ColorRGBa? = ColorRGBa.BLACK,

    // color
    var backgroundColor: ColorRGBa? = Colors.CLICKABLE,
    var hoverColor: ColorRGBa? = Colors.HOVERED,
    var focusColor: ColorRGBa? = Colors.FOCUSED,
    var selectColor: ColorRGBa? = Colors.SELECTED,
    var strokeColor: ColorRGBa? = Colors.BLACK,

    // other
    var strokeWeight: Double = 1.0,
    var borderRadius: Double = 2.0
) {

    val font by lazy {
        val context = UIHost.current.context
        val fonts = context.fonts
        val program = context.program
        val size = "%.1f".format(fontSize)
        val key = "$fontFamily-$size"
        val file = File(fontFamily.path, "${fontFamily.name}.${fontFamily.format}")

        return@lazy fonts.getOrPut(key) {
            program.loadFont(file.invariantSeparatorsPath, size.toDouble())
        }
    }

    companion object {
        fun fromHocon(path: String) {
            File(path)
        }
    }

}

class StyleStack() {

}

class StyleBuilder {
    val tree: UITree get() = UIHost.current.tree

    private val rules = mutableListOf<Pair<String, Style.() -> Unit>>()

    operator fun String.invoke(block: Style.() -> Unit = {}) {
        rules.add(this to block)
    }

    fun apply() {
        val all = tree.flattened + tree.widgets.flatMap { it.elements }
        for (el in all) {
            for ((selector, block) in rules) {
                if (matches(selector, el)) {
                    el.style.block()
                }
            }
        }
    }

    private fun matches(selector: String, el: UIElement): Boolean = when (selector) {
        "*" -> true
        else -> selector in el.selectors
    }
}

fun style(block: StyleBuilder.() -> Unit) {
    StyleBuilder().apply(block).apply()   // collect, then apply
}


fun main() {
    application {
        program {

            extend(UI())


            widget(drawer.bounds.offsetEdges(-10.0)) {
                gridSlots(1, 2, gutterX = 10.0, gutterY = 10.0) {
                    button(it[0]) {
                        selectors = listOf("one", "two", "three")
                    }
                    button(it[1]) {
                        selectors = listOf("two", "four")
                    }
                }
            }


            style {
                "one" {
                    strokeColor = ColorRGBa.RED
                }

                "two" {
                    borderRadius = 20.0
                    backgroundColor = ColorRGBa.YELLOW
                }

                "three" {
                    backgroundColor = ColorRGBa.BLUE
                }
            }
        }
    }
}


