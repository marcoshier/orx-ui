import org.openrndr.Program
import style.StyleStack

class UIHost(program: Program) {
    val context = UIContext(program)
    val tree = UITree()
    val style = StyleStack()

    companion object {
        lateinit var current: UIHost
            internal set

        val context: UIContext get() = current.context
    }
}

fun mousePosition() = UIHost.current.context.mouse.position
fun window() = UIHost.current.context.window
fun seconds() = UIHost.current.context.program.seconds
fun clipboard() = UIHost.current.context.program.clipboard