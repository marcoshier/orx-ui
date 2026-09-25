package style

import org.openrndr.Program
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.MinifyingFilter
import org.openrndr.draw.loadFont

class Fonts {
    companion object {

        fun load(fontName: String, fontSize: Double): FontImageMap {
            val p = Program.active!!

            return p.loadFont("data/fonts/$fontName", fontSize, contentScale = p.window.contentScale).also {
                it.texture.filter(MinifyingFilter.NEAREST, MagnifyingFilter.NEAREST)
                it.texture.generateMipmaps()
            }
        }

        val DEFAULT = load("default.otf", 16.0)
    }
}