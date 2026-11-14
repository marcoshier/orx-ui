import elements.TextInput
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import panel.ParameterPanel
import style.*
import ui.*
import widget.clipBounds
import java.io.File

private val logger = KotlinLogging.logger {  }

class UI: Extension {
    override var enabled = true

    lateinit var host: UIHost
        private set

    var activeElement: UIElement? = null
    var focusElement: UIElement? = null
    var dragElement: UIElement? = null
    var textElement: UIElement? = null
    var hoverElement: UIElement? = null
    var lastClickElement: UIElement? = null

    var lastClicked = 0.0
    var lastClickPosition = Vector2.ZERO
    val doubleClickThreshold = 0.3
    var pressTime = 0.0
    var hoverTime = 0.0
    var pressPosition = Vector2.ZERO

    var descriptionElementStyle = Style()

    var extensionStage = ExtensionStage.AFTER_DRAW

    val parameters by lazy { ParameterPanel(this) }

    var dragCursorHide = true

    fun setupAutoSave(save: () -> File) {
        host.program.ended.listen {
            val file = save()
            logger.info { "UI Auto-saved ${file.path}" }
        }
    }

    fun setCursor(type: CursorType) {
        UIHost.current.program.application.cursorType = type
    }

    fun setCursorVisibility(boolean: Boolean) {
        if (!dragCursorHide) return
        UIHost.current.program.application.cursorVisible = boolean
    }

    fun loadStylesheet(file: File): Stylesheet {
        if (!file.exists()) {
            logger.warn { "File $file not found" }
            return emptyStylesheet()
        }
        val sheet = Stylesheet.fromFile(file)
        applyStylesheet(sheet)
        return sheet
    }

    fun <T : Any> addParameters(obj: T, key: String? = null): T = parameters.add(obj, key)

    private fun requestDraw() {
        host.window.requestDraw()
    }

    private fun handleButtonDown(event: MouseEvent) {
        if (!event.propagationCancelled) {
            val tree = host.tree

            for (el in tree.hitOrder) {
                if (!el.visible()) continue
                if (!el.interactable()) continue
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
                    dragElement = null
                    pressPosition = event.position
                    pressTime = host.program.seconds

                    if (activeElement?.acceptsText == true) {
                        textElement = activeElement
                    }

                    el.isFocused = true
                    el.isActive = true
                    for (other in host.tree.flattened) {
                        if (other !== el) other.isFocused = false
                    }
                    break
                }
            }

            if (activeElement == null) {
                focusElement?.isFocused = false
                focusElement = null
            }
        }
    }

    private fun handleMouseMoved(event: MouseEvent) {
        if (activeElement != null) return

        val tree = host.tree
        var hit: UIElement? = null

        for (el in tree.hitOrder) {
            if (!el.visible() || !el.interactable()) continue
            if (hit == null && el.contains(event.position)) {
                hit = el
            }
        }

        if (hit == null) {
            hoverElement = null
        }

        for (el in tree.flattened) {
            val isHovered = el === hit
            if (isHovered && el.interactable()) {
                hoverElement = el
                hoverTime = seconds()
            }

            if (el.isHovered != isHovered) {
                el.isHovered = isHovered
                el.hovered.trigger(isHovered)
                requestDraw()
            }
        }

        if (hoverElement != null) {
            if (hoverElement!!.acceptsText) {
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
            event.cancelPropagation()
            return
        }

        if (event.propagationCancelled) return

        val widget = leafWidget(event.position) ?: return
        val speed = 30.0
        if (widget.yScrollable) {
            widget.yOffset += event.rotation.y * speed
            event.cancelPropagation()
        }
        if (widget.xScrollable) {
            widget.xOffset += event.rotation.y * speed
            event.cancelPropagation()
        }
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
                setCursorVisibility(false)
                dragElement?.dragged?.trigger(transformedEvent)
                requestDraw()
            }
        }
    }

    private fun handleButtonUp(event: MouseEvent) {
        if (!event.propagationCancelled) {
            if (activeElement != null) {
                val transformedEvent = transformMouseEvent(event, activeElement!!)
                setCursorVisibility(true)
                activeElement?.buttonUp?.trigger(transformedEvent)
                requestDraw()

                val elapsed = host.program.seconds - pressTime
                if (elapsed < 0.25) {
                    val now = host.program.seconds
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
                activeElement?.isActive = false
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
        host = UIHost(program)
        UIHost.current = host
        host.styleStack.add(StyleLibrary.DEFAULT, StyleLayer.DEFAULTS)

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

        host.tree.elementClosed.listen {
            if (focusElement === it) focusElement = null
            if (activeElement === it) activeElement = null
            if (dragElement === it) dragElement = null
            if (textElement === it) textElement = null
            if (lastClickElement === it) lastClickElement = null
        }
    }


    fun applyStylesheet(sheet: Stylesheet) = host.styleStack.add(sheet, StyleLayer.USER)

    private fun draw(drawer: Drawer) {
        drawer.isolated {
            drawer.defaults()
            for (widget in host.tree.widgets.sortedBy { it.zIndex }) {
                widget.draw(drawer, focusElement)
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

            drawer.isolated {
                focusElement?.let { el ->
                    if (el.parent?.clip == true) {
                        drawer.drawStyle.clip = el.parent?.clipBounds()
                    }
                    drawer.isolated {
                        el.updateAnimations()
                        drawer.translate(el.accumulatedOffset())
                        with(el) {
                            beforeDraw()
                            draw(drawer)
                            afterDraw()
                        }
                    }
                }
            }

            if (hoverElement != null && (seconds() - hoverTime) > 1.5 && hoverElement?.description != null) {
                var bb = hoverElement!!.bounds

                fun drawText(visible: Boolean): Rectangle {
                    drawer.fontMap = descriptionElementStyle.fontMap
                    drawer.fill = ColorRGBa.BLACK
                    return drawer.writer {
                        val start = bb.corner.y
                        box = bb.offsetEdges(-10.0)
                        newLine()
                        text(hoverElement?.description!!, visible)
                        val end = cursor.y + drawer.fontMap!!.ascenderLength
                        val h = end - start
                        Rectangle(mousePosition() - Vector2(0.0, h), 250.0, h)
                    }
                }

                bb = drawText(false)

                drawer.strokeWeight = descriptionElementStyle.strokeWeight
                drawer.stroke = descriptionElementStyle.strokeColor
                drawer.fill = descriptionElementStyle.backgroundColor
                drawer.rectangle(bb)

                drawText(true)
            }
        }

    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        if (extensionStage == ExtensionStage.BEFORE_DRAW) {
            draw(drawer)
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        if (extensionStage == ExtensionStage.AFTER_DRAW) {
            draw(drawer)
        }
    }
}


fun transformMouseEvent(event: MouseEvent, element: UIElement) = event.copy(
    position = transformPos(event.position, element),
    dragDisplacement = event.dragDisplacement * element.precision.toDouble()
)
fun UIElement.accumulatedOffset(): Vector2 {
    var dx = 0.0
    var dy = 0.0

    ancestors().forEachIndexed { i, w ->
        if (!this.fixed || i > 0) {
            dx += w.smoothXoffset
            dy += w.smoothYoffset
        }
        dx += w.dragOffset.x
        dy += w.dragOffset.y
    }

    return Vector2(dx, dy)
}

fun transformPos(position: Vector2, element: UIElement): Vector2 {
    return position - element.accumulatedOffset()
}

fun UIElement.contains(position: Vector2): Boolean {
    val transformedPos = transformPos(position, this)
    if (transformedPos !in bounds) return false

    val widget = parent ?: return true
    if (!widget.clip) return true

    return position in widget.clipBounds()
}