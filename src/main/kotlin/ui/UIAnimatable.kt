package ui

import org.openrndr.animatable.Animatable

open class UIAnimatable: Animatable() {
    context(element: UIElement)
    open fun update() { updateAnimation() }
}

fun UIElement.updateAnimations() {
    animations.forEach {
        context(this) {
            it.updateAnimation()
            it.update()
        }
    }
}