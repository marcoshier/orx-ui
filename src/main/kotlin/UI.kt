import org.openrndr.CursorType
import org.openrndr.Extension
import org.openrndr.MouseEvent
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import ui.UIElement
import widgets.draw

class UI: Extension {
    override var enabled = true

    lateinit var host: UIHost
        private set

    var activeElement: UIElement? = null
    var dragElement: UIElement? = null

    var lastClicked = 0.0
    var pressTime = 0.0
    var pressPosition = Vector2.ZERO

    fun setCursor(type: CursorType) {
        Program.active!!.application.cursorType = type
    }

    private fun requestDraw() {
        host.context.window.requestDraw()
    }


    private fun handleButtonDown(event: MouseEvent) {
        if (!event.propagationCancelled) {
            val tree = host.tree

            for (el in tree.hitOrder) {
                if (!el.visible) continue
                if (!el.interactable) continue
                if (!el.contains(event.position)) continue

                val transformedEvent = transformMouseEvent(event, el)

                el.buttonDown.trigger(transformedEvent)
                requestDraw()

                if (transformedEvent.propagationCancelled) {
                    event.cancelPropagation()
                }

                if (event.propagationCancelled) {
                    activeElement = el
                    dragElement = null
                    pressPosition = event.position
                    pressTime = host.context.program.seconds
                    for (other in host.tree.flattened) {
                        if (other !== el) other.isFocused = false
                    }
                    break
                }
            }
        }
    }

    private fun handleMouseMoved(event: MouseEvent) {
        if (activeElement != null) return

        val tree = host.tree
        var hit: UIElement? = null

        for (el in tree.hitOrder) {
            if (!el.visible || !el.interactable) continue
            if (hit == null && el.contains(event.position)) {
                hit = el
            }
        }

        for (el in tree.flattened) {
            val nowHovered = el === hit
            if (el.isHovered != nowHovered) {
                el.isHovered = nowHovered
                el.hovered.trigger(nowHovered)
                requestDraw()
            }
        }
    }

    private fun handleDrag(event: MouseEvent) {
        if (!event.propagationCancelled) {

            if (activeElement != null && event.position.distanceTo(pressPosition) > 4.0) {
                dragElement = activeElement
                dragElement?.isDragged = true
            }

            if (dragElement != null) {
                val transformedEvent = transformMouseEvent(event, dragElement!!)
                dragElement?.dragged?.trigger(transformedEvent)
                requestDraw()
            }
        }
    }

    private fun handleButtonUp(event: MouseEvent) {
        if (!event.propagationCancelled) {
            if (activeElement != null) {
                val transformedEvent = transformMouseEvent(event, activeElement!!)
                activeElement?.buttonUp?.trigger(transformedEvent)
                requestDraw()

                val elapsed = host.context.program.seconds - pressTime
                if (elapsed < 0.25) {
                    activeElement?.clicked?.trigger(transformedEvent)
                    lastClicked = host.context.program.seconds
                }
            }

            dragElement?.isDragged = false
            activeElement?.isDragged = false
            dragElement = null
            activeElement = null
        }
    }

    override fun setup(program: Program) {
        host = UIHost(program)
        UIHost.current = host
        UIRegistry.tree = host.tree

        program.mouse.apply {
            buttonDown.listen(::handleButtonDown)
            dragged.listen(::handleDrag)
            buttonUp.listen(::handleButtonUp)
            moved.listen(::handleMouseMoved)
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        for (widget in host.tree.widgets) {
            context(host) {
                widget.draw()
            }
        }

        for (element in host.tree.elements) {
            if (!element.isFocused) element.draw(drawer)
        }
        for (element in host.tree.elements) {
            if (element.isFocused) element.draw(drawer)
        }
    }
}


fun transformMouseEvent(event: MouseEvent, element: UIElement) = event.copy(
    position = transformPos(event.position, element)
)

fun transformPos(position: Vector2, element: UIElement): Vector2 {
    return Vector2(position.x, position.y - element.yOffset)
}

fun UIElement.contains(position: Vector2): Boolean {
    val transformedPos = transformPos(position, this)
    return transformedPos in bounds
}

