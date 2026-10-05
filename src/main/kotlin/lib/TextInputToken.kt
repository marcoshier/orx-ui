package lib

import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.extra.textwriter.TextWriter
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import kotlin.math.absoluteValue

sealed interface TextInputToken {
    val text: String
    var bounds: Rectangle
    var glyphOutputRectangles: List<Rectangle>
    var startIndex: Int

    fun rectangle(pos: Vector2): Rectangle? =
        glyphOutputRectangles.minByOrNull { it.position(1.0, 0.5).distanceTo(pos) }

    fun hit(pos: Vector2) = pos in bounds

    context(writer: TextWriter)
    fun draw(drawer: Drawer, visible: Boolean) {
        writer.text(text, visible)
        glyphOutputRectangles = writer.glyphOutput.rectangles.map { it.second }

        val fm = drawer.fontMap!!
        val b = if (glyphOutputRectangles.isNotEmpty()) glyphOutputRectangles.bounds else Rectangle.EMPTY
        val h = fm.ascenderLength + fm.descenderLength.absoluteValue
        val w = if (this is SpaceToken) {
            val fim = (drawer.fontMap!! as FontImageMap)
            (fim.glyphMetrics[' ']?.advanceWidth ?: 0.0) + writer.style.tracking
        } else b.width

        bounds = Rectangle(b.x, writer.cursor.y.coerceAtLeast(h * 2), w, h)
    }
}

data class WordToken(
    override var text: String = "",
    override var startIndex: Int = 0,
) : TextInputToken {
    override var bounds = Rectangle.EMPTY
    override var glyphOutputRectangles = listOf<Rectangle>()
}

data class SpaceToken(
    override val text: String = " ",
    override var startIndex: Int = 0,
) : TextInputToken {
    override var bounds = Rectangle.EMPTY
    override var glyphOutputRectangles = listOf<Rectangle>()
}

fun List<TextInputToken>.join() = joinToString("") { it.text }

fun String.toTextInputTokens(): List<TextInputToken> {
    val out = mutableListOf<TextInputToken>()
    var i = 0
    while (i < length) {
        if (this[i] == ' ') {
            out += SpaceToken(startIndex = i)
            i += 1
        } else {
            val start = i
            while (i < length && this[i] != ' ') i++
            out += WordToken(substring(start, i), startIndex = start)
        }
    }
    return out
}

val numberCharacters = Regex("^[0-9.,]*$")
val allCharacters = Regex("^[\\s\\S]*$")