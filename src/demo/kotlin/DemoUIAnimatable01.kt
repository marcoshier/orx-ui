import elements.button
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.shape.Rectangle
import ui.UIAnimatable
import ui.UIElement

fun main() = application {
    program {
        val ui = extend(UI())

        class SpawnAnimation: UIAnimatable() {
            var t = 0.01

            fun fadeIn() {
                t = 0.01
                ::t.animate(1.0, 500, Easing.CubicInOut)
            }

            context(element: UIElement)
            override fun update() {
                element.bounds = element.boundsCopy.scaledBy(t)
            }
        }

        button(Rectangle.fromCenter(drawer.bounds.center, 50.0)) {
            label = ""
            animations = listOf(SpawnAnimation().apply { fadeIn() })
            action = { (animations[0] as SpawnAnimation).fadeIn() }
        }
    }
}


