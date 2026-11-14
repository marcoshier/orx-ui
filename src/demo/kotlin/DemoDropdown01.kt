import elements.dropdown
import lib.selectableListOf
import lib.selectableMutableListOf
import org.openrndr.application
import org.openrndr.math.Vector2

fun main() {
    application {
        program {
            extend(UI())

            val state = object {
                var list = selectableListOf("A", "B", "C")
                var mlist = selectableMutableListOf(1, 2, 3)
            }

            val r = drawer.bounds.offsetEdges(-10.0).copy(height = 60.0)

            dropdown(state::list, r)
            dropdown(state::mlist, r.movedBy(Vector2(0.0, height / 2.0)))
        }
    }
}