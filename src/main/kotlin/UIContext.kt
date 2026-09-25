import org.openrndr.Program
import org.openrndr.draw.Drawer
import style.Colors
import style.Fonts

class UIContext(
    val program: Program,
) {
    val drawer: Drawer get() = program.drawer
    val mouse get() = program.mouse
    val window get() = program.window
}