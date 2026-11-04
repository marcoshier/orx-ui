import elements.dropdown
import lib.selectableListOf
import lib.selectableMutableListOf
import org.openrndr.application
import org.openrndr.panel.collections.SelectableList
import widget.widget

fun main() {
    application {
        program {
            extend(UI())

            val state = object {
                var list = selectableListOf("A", "B", "C")
                var mlist = selectableMutableListOf(1, 2, 3)
            }

            dropdown(drawer.bounds.offsetEdges(-10.0).copy(height = 40.0), state::list)
            dropdown(drawer.bounds.offsetEdges(-10.0).copy(height = 40.0), state::mlist)
        }
    }
}