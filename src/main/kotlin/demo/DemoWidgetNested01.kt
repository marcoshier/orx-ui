package demo

import UI
import elements.button
import lib.copy
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import widgets.Direction
import widgets.widget

fun main() {
    application {
        configure {
            width = 480
            height = 640
        }
        program {
            extend(UI())

            widget(drawer.bounds.offsetEdges(-10.0)) {
                minHeight = 80.0
                gutterY = 10.0
                padding = Vector2(20.0, 10.0)

                widget(bounds.scaledBy(1.0, 0.5, 0.5, 0.0).offsetEdges(-5.0)) {
                    this.backgroundColor = ColorRGBa.WHITE.shade(0.9)

                    repeat(10) { i ->
                        append(Direction.DOWN) { button { label = "A$i" } }
                    }

                }

                widget(bounds.scaledBy(1.0, 0.5, 0.5, 1.0).offsetEdges(-5.0)) {
                    this.backgroundColor = ColorRGBa.WHITE.shade(0.9)

                    repeat(2) { i ->
                        append(Direction.DOWN) { button { label = "B$i" } }
                    }

                    append(Direction.DOWN) {
                        widget(bounds.copy(y = itemY, height = 200.0)) {
                            this.backgroundColor = parent?.style?.backgroundColor?.shade(0.8)
                            minWidth = bounds.width - paddingX * 2
                            gutterX = 10.0
                            repeat(10) { j ->
                                append(Direction.RIGHT) { button { label = "BB$j" } }
                            }
                        }
                    }

                    repeat(8) { i ->
                        append(Direction.DOWN) { button { label = "B${i + 2}" } }
                    }

                }
            }
        }
    }
}