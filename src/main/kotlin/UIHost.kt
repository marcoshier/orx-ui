import org.openrndr.Program

class UIHost(program: Program) {
    val context = UIContext(program)
    val tree = UITree()

    companion object {
        lateinit var current: UIHost
            internal set

        val context: UIContext get() = current.context
    }
}

fun mousePosition() = UIHost.current.context.mouse.position
fun window() = UIHost.current.context.window