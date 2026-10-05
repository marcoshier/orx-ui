package demo

import UI
import elements.textboxInput
import elements.textInput
import org.openrndr.application
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

fun main() {
    application {
        configure {
            width = 250
        }
        program {
            extend(UI())

            val state = object {
                var text = "Hi! I am a single-line text. Cut me, scroll me, paste me, replace me."
                var text2 = "This is a multi-line text-box. I am resizable (drag bottom corner)." +
                        "You can use me like any traditional textbox. Double click on words to select them," +
                        "SHIFT+arrows to select characters, CTRL+A to select all, CTRL+C to copy" +
                        "CTRL+X to cut, CTRL+V to paste. Scroll with mouse and arrow buttons."
            }

            textInput("text", Vector2(10.0), width - 20.0, state::text)
            textboxInput("text2", Rectangle(10.0, 70.0, width - 20.0, 150.0), state::text2)
        }
    }
}