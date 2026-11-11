package ui

import org.openrndr.MouseEvent
import org.openrndr.math.Vector2

fun UIElement.close() {
    val elRef = this@close
    deregisterElement(elRef)
    UIHost.current.tree.elementClosed.trigger(this)
}

context(host: UIHost)
fun UIElement.draw() {
    val elRef = this@draw
    elRef.draw(host.drawer)
}

fun UIElement.makeDraggable(
    onButtonDown: (MouseEvent) -> Unit = {},
    onDragged: (MouseEvent) -> Unit = {},
    onButtonUp: (MouseEvent) -> Unit = {}
) {
    buttonDown.listen {
        it.cancelPropagation()
        dragPosition = it.position
        onButtonDown(it)
    }

    dragged.listen {
        val delta = it.position - dragPosition
        bounds = bounds.movedBy(delta)
        dragPosition = it.position
        onDragged(it)
    }

    buttonUp.listen {
        dragPosition = Vector2.ZERO
        onButtonUp(it)
    }
}
