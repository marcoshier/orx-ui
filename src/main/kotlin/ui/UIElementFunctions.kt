package ui

import UIHost
import deregisterElement

fun UIElement.close() {
    val elRef = this@close
    deregisterElement(elRef)
}

context(host: UIHost)
fun UIElement.draw() {
    val elRef = this@draw
    elRef.draw(host.context.drawer)
}