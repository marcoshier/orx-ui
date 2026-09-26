package demo

import UI
import UIHost
import elements.button
import org.openrndr.application
import org.openrndr.shape.Rectangle

fun main() = application {
    program {
        extend(UI())

        button("Click me", Rectangle(10.0, 10.0, 100.0)) {
            action = { println("clicked") }
        }
    }
}


