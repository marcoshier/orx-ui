package elements

import clipboard
import deregisterElement
import io.github.oshai.kotlinlogging.KotlinLogging
import lib.TextInputToken
import lib.allCharacters
import lib.join
import lib.toTextInputTokens
import org.openrndr.KEY_ARROW_DOWN
import org.openrndr.KEY_ARROW_LEFT
import org.openrndr.KEY_ARROW_RIGHT
import org.openrndr.KEY_ARROW_UP
import org.openrndr.KEY_BACKSPACE
import org.openrndr.KEY_END
import org.openrndr.KEY_HOME
import org.openrndr.KeyModifier
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shadestyles.fills.gradients.gradient
import org.openrndr.extra.shapes.primitives.roundedRectangle
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.textwriter.writer
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import registerElement
import seconds
import style.Colors
import ui.UIElementImpl
import widgets.WidgetBuilder
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.reflect.KMutableProperty0

private val logger = KotlinLogging.logger {  }

class TextInput(
    label: String = "",
    bounds: Rectangle,
    private val getter: () -> String,
    private val setter: (String) -> Unit,
    configure: TextInput.() -> Unit = {}
): UIElementImpl(label, bounds) {

    constructor(
        label: String,
        bounds: Rectangle,
        textRef: KMutableProperty0<String>,
        configure: TextInput.() -> Unit = {}
    ): this(label, bounds, { textRef.get() }, { textRef.set(it) }, configure)

    var maxCharacters = Int.MAX_VALUE
    var allowedCharacters = allCharacters
    var placeholderText = "Text here..."

    var multiline = false
    var resizable = false
        set(value) {
            if (!multiline) return
            if (!field && value && resizeControl == null) {
                val b = resizeButton()
                registerElement(b)
                resizeControl = b
            } else if (field && !value && resizeControl != null) {
                deregisterElement(resizeControl!!)
                resizeControl = null
            }
            field = value
        }

    var tokens = listOf<TextInputToken>()
        private set

    private var resizeControl: Button? = null
    private val resizeControlBounds: Rectangle
        get() = Rectangle(bounds.x + bounds.width - 16.0, bounds.y + bounds.height - 15.0, 14.0)

    private var selectionStart: Int? = null
    private var selectionEnd: Int? = null
    private val hasSelection: Boolean
        get() = selectionStart != null && selectionEnd != null && selectionStart != selectionEnd

    var caretIdx: Int? = null
        private set

    val caretPosition: Vector2
        get() = currentCaretPosition()

    val contentWidth: Double
        get() = tokens.map { it.bounds }.bounds.width

    val contentHeight: Double
        get() = tokens.map { it.bounds }.bounds.height

    private var offset = 0.0
        set(value) {
            field = value.coerceIn(-maxOffset, 0.0)
        }

    private val offsetAxis: Vector2
        get() = if (multiline) Vector2.UNIT_Y else Vector2.UNIT_X

    private val maxOffset: Double
        get() {
            return if (multiline) {
                contentHeight - bounds.height + 15.0
            } else {
                contentWidth - bounds.width - 15.0
            }.coerceAtLeast(0.0)
        }

    private fun transformedPosition(pos: Vector2): Vector2 {
        return pos - (offsetAxis * offset)
    }

    private fun insertAtCaretPosition(s: String) {
        val t = getter()
        val at = caretIdx ?: t.length
        val new = t.substring(0, at) + s + t.substring(at)
        setter(new)
        caretIdx = (at + s.length).coerceIn(0, getter().length)
        clearSelection()
    }

    private fun currentCaretPosition(): Vector2 {
        val cgr = caretGlyphRect()
        return if (cgr != null) {
            val after = cgr.after
            val uv = if (after) Vector2.ONE else Vector2(0.0, 1.0)
            cgr.glyphRectangle.position(uv)
        } else Vector2(bounds.x + 7.0, bounds.y + 7.0 + style.font.height)
    }

    private fun backspace() {
        val t = getter()

        if (hasSelection) {
            deleteSelection()
            return
        }

        val at = caretIdx ?: t.length
        if (at <= 0) return

        val new = t.substring(0, at - 1) + t.substring(at)
        setter(new)
        caretIdx = (at - 1).coerceIn(0, getter().length)
    }

    fun followCaret() {
        if (multiline) {
            when(caretPosition.y + offset) {
                in Double.MIN_VALUE..bounds.y + 7.0 -> offset = lines()[0][0].bounds.height
                in bounds.y + bounds.height..Double.MAX_VALUE -> offset -= 20.0
            }
        } else {
            when(caretPosition.x + offset) {
                in Double.MIN_VALUE..bounds.x + 7.0 -> offset += 20.0
                in bounds.x + bounds.width..Double.MAX_VALUE -> offset -= 20.0
            }
        }
    }

    private fun moveCaret(to: Int, extend: Boolean) {
        val target = to.coerceIn(0, getter().length)

        if (extend) {
            if (selectionStart == null) {
                selectionStart = caretIdx ?: getter().length
            }
            selectionEnd = target
        } else {
            clearSelection()
        }

        caretIdx = target
        followCaret()
    }

    private fun selectionRectangles(): List<Rectangle> {
        if (!hasSelection) return emptyList()

        val min = min(selectionStart!!, selectionEnd!!)
        val max = max(selectionStart!!, selectionEnd!!)

        val letterRects = mutableListOf<Rectangle>()
        for (t in tokens) {
            val s = t.startIndex
            val e = t.startIndex + t.text.length
            if (s >= max || e <= min) continue

            val from = (min - s).coerceIn(0, t.text.length)
            val until = (max - s).coerceIn(0, t.text.length)
            if (from >= until) continue

            val glyphs = t.glyphOutputRectangles

            if (glyphs.isEmpty()) {
                if (t.bounds != Rectangle.EMPTY) {
                    letterRects.add(t.bounds)
                }
            } else {
                val sl = glyphs.subList(
                    from.coerceIn(0, glyphs.size),
                    until.coerceIn(0, glyphs.size)
                )

                for (r in sl) {
                    letterRects.add(
                        Rectangle(r.x, t.bounds.y, r.width, t.bounds.height)
                    )
                }
            }
        }

        if (letterRects.isEmpty())
            return emptyList()

        val lines = letterRects
            .groupBy { (it.y / it.height).roundToInt() }
            .values.toList()

        return lines.map { rects ->
            val x0 = rects.minOf { it.x }
            val x1 = rects.maxOf { it.x + it.width }
            val top = rects.minOf { it.y }
            Rectangle(x0, top, (x1 - x0).coerceAtLeast(0.0), rects[0].height)
        }
    }

    private fun clearSelection() {
        selectionStart = null
        selectionEnd = null
    }

    private fun deleteSelection() {
        val s = selectionStart ?: return
        val e = selectionEnd ?: return
        val min = min(s, e)
        val max = max(s, e)

        val t = getter()
        val new = t.substring(0, min) + t.substring(max)

        setter(new)
        caretIdx = min.coerceIn(0, getter().length)
        clearSelection()
    }

    private fun caretIndexAt(position: Vector2): Int {
        val hit = tokens.lastOrNull { it.hit(position) }

        fun glyphDistance(token: TextInputToken): Int {
            val glyphs = token.glyphOutputRectangles

            var nearestIdx = 0
            var nearestDist = Double.MAX_VALUE
            var after = false

            glyphs.forEachIndexed { gi, g ->
                val dl = g.position(0.0, 0.5).distanceTo(position)
                val dr = g.position(1.0, 0.5).distanceTo(position)

                if (dl < nearestDist) {
                    nearestDist = dl
                    nearestIdx = gi
                    after = false
                }

                if (dr < nearestDist) {
                    nearestDist = dr
                    nearestIdx = gi
                    after = true
                }
            }

            val endIndex = if (after) 1 else 0
            return (token.startIndex + nearestIdx + endIndex).coerceIn(0, getter().length)
        }

        if (hit != null) {
            val glyphs = hit.glyphOutputRectangles
            if (glyphs.isEmpty()) {
                val mid = hit.bounds.x + hit.bounds.width / 2.0
                val endIndex = if (position.x > mid) 1 else 0
                return (hit.startIndex + endIndex).coerceIn(0, getter().length)
            }

            return glyphDistance(hit)
        } else {
            val l = tokens.lastOrNull() ?: return 0
            val gt = position.x > l.bounds.x + l.bounds.width
                    && position.y > l.bounds.y

            val nearestToken = if (!gt) tokens.minByOrNull { it.bounds.center.distanceTo(position) } else l

            if (nearestToken != null) {
                return glyphDistance(nearestToken)
            }
        }

        return 0
    }

    data class Caret(val glyphRectangle: Rectangle, val after: Boolean)

    private fun caretGlyphRect(): Caret? {
        val at = caretIdx ?: getter().length

        for (t in tokens) {
            val s = t.startIndex
            val e = t.startIndex + t.text.length
            val glyphs = t.glyphOutputRectangles
            if (at in s..e && glyphs.isNotEmpty()) {
                val gi = at - s
                return if (gi < glyphs.size) Caret(glyphs[gi], false)
                else Caret(glyphs.last(), true)
            }
        }

        val left = tokens.filter { it.bounds != Rectangle.EMPTY && it.startIndex + it.text.length <= at }
            .maxByOrNull { it.startIndex }
        if (left != null && left.glyphOutputRectangles.isNotEmpty()) {
            return Caret(left.glyphOutputRectangles.last(), true)
        }

        val right = tokens.filter { it.bounds != Rectangle.EMPTY && it.startIndex >= at }
            .minByOrNull { it.startIndex }
        if (right != null && right.glyphOutputRectangles.isNotEmpty()) {
            return Caret(right.glyphOutputRectangles.first(), false)
        }

        return null
    }

    fun lines(): List<List<TextInputToken>> {
        val g = tokens.groupBy { (it.bounds.y / it.bounds.height).toInt() }
        return g.values.toList()
    }

    fun resize(dragPosition: Vector2) {
        bounds = bounds.copy(
            width = (dragPosition.x - bounds.x).coerceAtLeast(20.0),
            height = (dragPosition.y - bounds.y).coerceAtLeast(20.0)
        )
        if (contentHeight < bounds.height) {
            offset = 0.0
        }
    }

    private fun resizeButton(): Button {
        return Button("", resizeControlBounds) {
            style.idleColor = this@TextInput.style.idleColor.shade(0.95)
            style.selectColor = style.idleColor.shade(0.95)
            style.focusColor = style.idleColor
            iconOff = { drawer ->
                drawer.stroke = style.hoverColor.shade(0.75)
                for (i in 0 until 3) {
                    val b = bounds.offsetEdges(-1.0)
                    val t = (i / 3.0)
                    val x = Vector2(t, 1.0)
                    val y = Vector2(1.0, t)
                    drawer.lineSegment(b.position(x), b.position(y))
                }
            }
            zIndex = this@TextInput.zIndex + 1
            dragAction = { resize(it.position) }
        }
    }

    init {
        acceptsText = true

        buttonDown.listen {
            if (it.position in resizeControlBounds) return@listen

            it.cancelPropagation()
            val pos = transformedPosition(it.position)
            val i = caretIndexAt(pos)
            caretIdx = i
            selectionStart = i
            selectionEnd = i
        }

        dragged.listen {
            val pos = transformedPosition(it.position)
            val i = caretIndexAt(pos)
            selectionEnd = i
            caretIdx = i
        }

        clicked.listen {
            if (!hasSelection) {
                val pos = transformedPosition(it.position)
                caretIdx = caretIndexAt(pos)
            }
        }

        doubleClicked.listen {
            val pos = transformedPosition(it.position)
            val token = tokens.firstOrNull { t -> t.hit(pos) }
            if (token != null) {
                selectionStart = token.startIndex
                selectionEnd = token.startIndex + token.text.length
                caretIdx = selectionEnd
            }
        }

        scrolled.listen {
            val dt = it.rotation.y * 5.0

            if (maxOffset > 0.0) {
                offset = (offset + dt)
                it.cancelPropagation()
            }
        }

        character.listen {
            if (it.modifiers.contains(KeyModifier.CTRL))
                return@listen

            if (!allowedCharacters.matches("${it.character}"))
                return@listen

            if (tokens.join().length >= maxCharacters)
                return@listen

            if (hasSelection) {
                deleteSelection()
            }
            insertAtCaretPosition(it.character.toString())
        }

        fun moveLine(direction: Int): Int {
            val r = caretGlyphRect() ?: return caretIdx ?: 0
            val p = r.glyphRectangle.center

            val lines = lines()
            val currentLine = lines.find { line ->
                p in line.map { it.bounds }.bounds
            }

            val lineIndex = lines.indexOf(currentLine)
            if (lineIndex == -1)
                return caretIdx ?: 0

            val targetLineIndex =
                (lineIndex + direction).coerceIn(0, lines.lastIndex)

            if (targetLineIndex == lineIndex)
                return caretIdx ?: 0

            val targetLine = lines[targetLineIndex]

            val nearestToken = targetLine.minByOrNull {
                (it.bounds.center.x - p.x).absoluteValue
            } ?: return caretIdx ?: 0

            val nearestGlyph = nearestToken.glyphOutputRectangles.minByOrNull {
                (it.center.x - p.x).absoluteValue
            } ?: return nearestToken.startIndex

            val glyphIndex =
                nearestToken.glyphOutputRectangles.indexOf(nearestGlyph)

            return nearestToken.startIndex + glyphIndex
        }

        keyDown.listen {
            val shift = it.modifiers.contains(KeyModifier.SHIFT)
            val at = caretIdx ?: getter().length
            when (it.key) {
                KEY_BACKSPACE -> backspace()
                KEY_ARROW_LEFT  -> moveCaret(at - 1, shift)
                KEY_ARROW_RIGHT -> moveCaret(at + 1, shift)
                KEY_ARROW_UP -> {
                    moveCaret(moveLine(-1), shift)
                }
                KEY_ARROW_DOWN -> {
                    moveCaret(moveLine(1), shift)
                }
                KEY_HOME -> {
                    caretIdx = 0
                    clearSelection()
                }
                KEY_END -> {
                    caretIdx = getter().length
                    clearSelection()
                }
                else -> {
                    if (it.modifiers.contains(KeyModifier.CTRL)) {
                        when(it.key.toChar()) {
                            'a' -> {
                                selectionStart = 0
                                selectionEnd = tokens.join().length
                                caretIdx = selectionEnd
                            }
                            'v' -> {
                                clipboard().contents?.let {
                                    if (tokens.join().length + it.length > maxCharacters) {
                                        logger.warn { "paste would exceed character limit" }
                                        return@listen
                                    }
                                    insertAtCaretPosition(it)
                                }
                            }
                            'c' -> {
                                if (selectionStart != null && selectionEnd != null) {
                                    val t = tokens.join()
                                    val min = min(selectionStart!!, selectionEnd!!)
                                    val max = max(selectionStart!!, selectionEnd!!)
                                    clipboard().contents = t.substring(min, max)
                                }
                            }
                            'x' -> {
                                if (selectionStart != null && selectionEnd != null) {
                                    val t = tokens.join()
                                    val min = min(selectionStart!!, selectionEnd!!)
                                    val max = max(selectionStart!!, selectionEnd!!)
                                    clipboard().contents = t.substring(min, max)
                                    deleteSelection()
                                }
                            }
                        }
                    }
                }
            }
        }

        configure()
    }

    override fun draw(drawer: Drawer) {
        super.draw(drawer)
        if (!visible) return

        drawer.pushStyle()
        drawer.drawStyle.clip = bounds
        drawer.stroke = null
        drawer.fill = style.idleColor
        drawer.roundedRectangle(bounds.toRounded(style.borderRadius))

        tokens = getter().toTextInputTokens()

        drawer.fontMap = style.font

        fun drawText(visible: Boolean) {
            drawer.isolated {
                translate(offsetAxis * offset)
                writer {
                    var textBox = this@TextInput.bounds.offsetEdges(-7.0)
                    if (multiline) textBox = textBox.copy(height = Double.MAX_VALUE)
                    else textBox = textBox.copy(width = Double.MAX_VALUE)
                    box = textBox
                    verticalAlign = if (multiline) 0.0 else 0.5

                    if (tokens.isEmpty()) {
                        text(placeholderText)
                    } else {
                        for (t in tokens) {
                            context(this@writer) {
                                t.draw(drawer, visible)
                            }
                        }
                    }
                }
            }
        }

        drawText(visible = false)

        for ((i, t) in tokens.withIndex()) {
            if (t.glyphOutputRectangles.isNotEmpty()) continue
            val prev = tokens.getOrNull(i - 1)?.bounds
            val next = tokens.getOrNull(i + 1)?.bounds
            if (prev != null && prev != Rectangle.EMPTY) {
                val x0 = prev.x + prev.width
                val x1 = next?.takeIf { it != Rectangle.EMPTY }?.x ?: (x0 + 5.0)
                t.bounds = Rectangle(x0, prev.y, (x1 - x0).coerceAtLeast(2.0), prev.height)
            }
        }

        drawer.stroke = when {
            isFocused -> style.selectColor
            !isFocused && isHovered -> style.hoverColor
            else -> style.idleColor.mix(style.hoverColor, 0.5)
        }

        val baselineY = if (multiline) {
            bounds.y + bounds.height - 7.0
        } else {
            bounds.y + bounds.height / 2.0 + drawer.fontMap!!.height
        }
        val p0 = Vector2(bounds.x + 7.0, baselineY)
        val end = if (multiline) 25.0 else 7.0
        val p1 = Vector2(bounds.x + bounds.width - end, baselineY)
        drawer.lineSegment(p0, p1)

        drawer.isolated {
            translate(offsetAxis * offset)
            selectionRectangles().forEach {
                drawer.stroke = null
                drawer.fill = style.hoverColor.opacify(0.5)
                drawer.rectangle(it)
            }
        }

        drawer.fill = if (tokens.isEmpty()) style.hoverColor else style.textColor
        drawText(visible = true)

        if (isFocused) {

            fun caretSegment(): Pair<Vector2, Vector2>? {
                val font = drawer.fontMap ?: return null

                val gr = caretGlyphRect()
                if (gr == null) {
                    val x = bounds.x + 7.0
                    val top = bounds.y + 7.0
                    return Vector2(x, top) to Vector2(x, top + font.height)
                }
                val (rect, after) = gr
                val x = if (after) rect.position(1.0, 0.5).x else rect.position(0.0, 0.5).x
                val baseline = rect.y + rect.height
                val top = baseline - font.ascenderLength
                val bottom = baseline + font.descenderLength
                return Vector2(x, top - 5.0) to Vector2(x, bottom + 5.0)
            }

            drawer.isolated {
                translate(offsetAxis * offset)
                val ls = caretSegment()
                ls?.let { (p0, p1) ->
                    val t = 1.0 - (seconds() * 0.765).mod(1.0)
                    drawer.stroke = ColorRGBa.BLACK.opacify(sin(t).absoluteValue * 2.0)
                    drawer.strokeWeight = 0.1
                    val off = Vector2.UNIT_X * 1.1
                    drawer.lineSegment(p0 + off, p1 + off)
                }
            }
        }

        fun drawGradient(rect: Rectangle, start: Vector2, end: Vector2) {
            drawer.isolated {
                drawer.stroke = null
                drawer.fill = ColorRGBa.WHITE
                drawer.shadeStyle = gradient<ColorRGBa> {
                    stops[0.0] = style.idleColor
                    stops[1.0] = style.idleColor.opacify(0.0)
                    linear {
                        this.start = start
                        this.end = end
                    }
                }
                drawer.roundedRectangle(rect.toRounded(2.0))
            }
        }

        if (multiline) {
            if (contentHeight > bounds.height) {
                if (offset != 0.0) {
                    drawGradient(Rectangle(bounds.corner, bounds.width, 30.0), Vector2(0.5, 0.0), Vector2(0.5, 1.0))
                }
                if ((offset.absoluteValue != maxOffset)) {
                    drawGradient(
                        Rectangle(
                            bounds.x,
                            bounds.y + bounds.height - 30.0,
                            bounds.width, 30.0
                        ), Vector2(0.5, 1.0), Vector2(0.5, 0.0)
                    )
                }
            }
        } else {
            if (contentWidth > bounds.width) {
                if (offset != 0.0) {
                    drawGradient(Rectangle(bounds.corner, bounds.height, bounds.height), Vector2(0.0, 0.5), Vector2(1.0, 0.5))
                }
                if (offset.absoluteValue != maxOffset) {
                    drawGradient(
                        Rectangle(
                            bounds.x + bounds.width - bounds.height,
                            bounds.y,
                            bounds.height, bounds.height
                        ), Vector2(1.0, 0.5), Vector2(0.0, 0.5)
                    )
                }
            }
        }

        resizeControl?.let { rc ->
            rc.bounds = resizeControlBounds
            rc.draw(drawer)
        }

        drawer.popStyle()
    }
}

fun textInput(
    label: String = "",
    corner: Vector2,
    width: Double,
    textRef: KMutableProperty0<String>,
    configure: TextInput.() -> Unit = {},
): TextInput {
    val b = TextInput(label, Rectangle(corner.x, corner.y, width, 50.0), textRef) {
        multiline = false
        configure()
    }
    registerElement(b)
    return b
}

fun WidgetBuilder.textInput(
    label: String = "",
    corner: Vector2,
    width: Double,
    textRef: KMutableProperty0<String>,
    configure: TextInput.() -> Unit = {},
): TextInput {
    val b = TextInput(label, Rectangle(corner.x, corner.y, width, 50.0), textRef) {
        multiline = false
        configure()
    }
    add(b)
    return b
}

fun textboxInput(
    label: String = "",
    bounds: Rectangle,
    textRef: KMutableProperty0<String>,
    configure: TextInput.() -> Unit = {},
): TextInput {
    val b = TextInput(label, bounds, textRef) {
        multiline = true
        resizable = true
        configure()
    }
    registerElement(b)
    return b
}

fun WidgetBuilder.textboxInput(
    label: String = "",
    bounds: Rectangle,
    textRef: KMutableProperty0<String>,
    configure: TextInput.() -> Unit = {},
): TextInput {
    val b = TextInput(label, bounds, textRef) {
        multiline = true
        resizable = true
        configure()
    }
    add(b)
    return b
}
