import elements.TextInput
import elements.button
import elements.checkbox
import elements.dropdown
import elements.slider
import elements.textInput
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openrndr.CharacterEvent
import org.openrndr.CursorType
import org.openrndr.Extension
import org.openrndr.ExtensionStage
import org.openrndr.KeyEvent
import org.openrndr.MouseEvent
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.parameters.ParameterType
import org.openrndr.extra.parameters.listParameters
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import style.Style
import style.StyleLibrary
import style.Stylesheet
import ui.UIElement
import ui.updateAnimations
import widgets.Direction
import widgets.Widget
import widgets.clipBounds
import widgets.widget
import java.io.File
import kotlin.reflect.KMutableProperty1

private val logger = KotlinLogging.logger {  }

class UI: Extension {
    override var enabled = true

    lateinit var host: UIHost
        private set

    var activeElement: UIElement? = null
    var focusElement: UIElement? = null
    var dragElement: UIElement? = null
    var textElement: UIElement? = null
    var lastClickElement: UIElement? = null

    var focusWidget: Widget? = null

    var lastClicked = 0.0
    var lastClickPosition = Vector2.ZERO
    val doubleClickThreshold = 0.3
    var pressTime = 0.0
    var pressPosition = Vector2.ZERO

    var extensionStage = ExtensionStage.AFTER_DRAW

    fun setCursor(type: CursorType) {
        Program.active!!.application.cursorType = type
    }

    fun loadStyle(style: Style) {
        host.style.default.apply(style)
    }

    fun loadStylesheet(file: File): Stylesheet {
        if (!file.exists()) {
            logger.warn { "File $file not found. Loading default style" }
        }
        host.style.fromStylesheet(Stylesheet.fromFile(file))
        return host.style.toStylesheet()
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
                    focusElement = activeElement
                    focusWidget = activeElement?.parent
                    dragElement = null
                    pressPosition = event.position
                    pressTime = host.context.program.seconds

                    if (activeElement?.acceptsText == true) {
                        textElement = activeElement
                    }

                    el.isFocused = true
                    for (other in host.tree.flattened) {
                        if (other !== el) other.isFocused = false
                    }
                    break
                }
            }

            if (activeElement == null) {
                focusElement?.isFocused = false
                focusElement = null
                focusWidget = null
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

        var hoveredElement: UIElement? = null

        for (el in tree.flattened) {
            val nowHovered = el === hit
            if (nowHovered) hoveredElement = el

            if (el.isHovered != nowHovered) {
                el.isHovered = nowHovered
                el.hovered.trigger(nowHovered)
                requestDraw()
            }
        }

        if (hoveredElement != null) {
            if (hoveredElement.acceptsText) {
                setCursor(CursorType.IBEAM_CURSOR)
            } else {
                setCursor(CursorType.HAND_CURSOR)
            }
        } else {
            setCursor(CursorType.ARROW_CURSOR)
        }
    }

    private fun handleScroll(event: MouseEvent) {
        if (focusElement is TextInput) {
            focusElement?.scrolled?.trigger(event)
            return
        }

        if (event.propagationCancelled) return

        val widget = leafWidget(event.position) ?: return
        val speed = 30.0
        if (widget.yScrollable) widget.yOffset += event.rotation.y * speed
        if (widget.xScrollable) widget.xOffset += event.rotation.y * speed
        requestDraw()
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
                    val now = host.context.program.seconds
                    val sinceLast = now - lastClicked
                    val inRange = event.position.distanceTo(lastClickPosition) < 8.0

                    if (sinceLast < doubleClickThreshold && inRange && activeElement === lastClickElement) {
                        activeElement?.doubleClicked?.trigger(transformedEvent)
                        lastClicked = 0.0
                        lastClickElement = null
                    } else {
                        activeElement?.clicked?.trigger(transformedEvent)
                        lastClicked = now
                        lastClickPosition = event.position
                        lastClickElement = activeElement
                    }
                }

                dragElement?.isDragged = false
                activeElement?.isDragged = false
                dragElement = null
                activeElement = null

            }
        }

    }

    private fun handleCharacter(event: CharacterEvent) {
        textElement?.character?.trigger(event)
    }

    private fun handleKeydown(event: KeyEvent) {
        textElement?.keyDown?.trigger(event)
    }

    override fun setup(program: Program) {
        host = UIHost(program)
        UIHost.current = host
        loadStyle(StyleLibrary.DEFAULT)
        UIRegistry.tree = host.tree

        program.mouse.apply {
            buttonDown.listen(::handleButtonDown)
            dragged.listen(::handleDrag)
            buttonUp.listen(::handleButtonUp)
            moved.listen(::handleMouseMoved)
            scrolled.listen(::handleScroll)
        }

        program.keyboard.apply {
            character.listen(::handleCharacter)
            keyDown.listen(::handleKeydown)
        }
    }

    private fun draw(drawer: Drawer, program: Program) {
        for (widget in host.tree.widgets.sortedBy { it.zIndex }) {
            if (widget !== focusWidget) {
                widget.draw(drawer)
            }
        }

        for (element in host.tree.elements) {
            if (element !== focusElement) {
                element.updateAnimations()
                with(element) {
                    beforeDraw()
                    draw(drawer)
                    afterDraw()
                }
            }
        }

        if (focusWidget != null) {
            focusWidget?.draw(drawer)
        } else {
            focusElement?.updateAnimations()
            focusElement?.let { el ->
                with(el) {
                    beforeDraw()
                    draw(drawer)
                    afterDraw()
                }
            }
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        if (extensionStage == ExtensionStage.BEFORE_DRAW) {
            draw(drawer, program)
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        if (extensionStage == ExtensionStage.AFTER_DRAW) {
            draw(drawer, program)
        }
    }
}


fun transformMouseEvent(event: MouseEvent, element: UIElement) = event.copy(
    position = transformPos(event.position, element)
)

fun transformPos(position: Vector2, element: UIElement): Vector2 {
    val dragOffset = element.parent?.dragOffset ?: Vector2.ZERO
    return Vector2(position.x - element.xOffset, position.y - element.yOffset) - dragOffset
}

fun UIElement.contains(position: Vector2): Boolean {
    val transformedPos = transformPos(position, this)
    if (transformedPos !in bounds) return false

    val widget = parent ?: return true
    if (!widget.clip) return true
    return position in widget.clipBounds()
}
