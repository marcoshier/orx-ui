import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap

class UIContext(
    val program: Program,
) {
    val drawer: Drawer get() = program.drawer
    val mouse get() = program.mouse
    val window get() = program.window

    val fonts: MutableMap<String, FontImageMap> = mutableMapOf()
}