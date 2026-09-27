package demo

import UI
import elements.Button
import elements.button
import lib.sdf3
import org.openrndr.animatable.Animatable
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import style.Colors
import ui.UIAnimatable
import ui.UIElement
import ui.UIElementImpl

fun main() = application {
    program {
        val ui = extend(UI())

        class SpawnAnimation: UIAnimatable() {
            var t = 0.01

            fun fadeIn() {
                ::t.animate(1.0, 500, Easing.CubicInOut)
            }

            context(element: UIElement)
            override fun update() {
                element.bounds = element.boundsCopy.scaledBy(t)
            }
        }


        fun placeButton(label: String, from: Rectangle) {
            button(label, from) {
                animations = listOf(SpawnAnimation().apply { fadeIn() })
                action = {
                    val existing = ui.host.tree.flattened.map { it.boundsCopy }

                    val size = Double.uniform(from.width * 0.4, from.width)
                    val angle = Int.uniform(0, 5) * 90.0
                    val step = from.width / 2.0 + size / 2.0 + 5.0
                    var candidate = Rectangle.fromCenter(
                        from.center + Polar(angle, step).cartesian,
                        size, size
                    )

                    repeat(100) {
                        val hit = existing.firstOrNull { it.sdf3(candidate).z < 0.0 }
                            ?: return@repeat
                        val push = hit.sdf3(candidate)
                        candidate = candidate.movedBy(Vector2(push.x, push.y))
                    }

                    val clear = existing.none { it.sdf3(candidate).z < 0.0 }
                    if (clear) placeButton(label, candidate)
                }
            }
        }

        placeButton("X", Rectangle.fromCenter(drawer.bounds.center, 50.0))
    }
}


