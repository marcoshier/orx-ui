package demo

import UI
import elements.rangeSlider
import org.openrndr.application
import widget.gridSlots
import widget.widget

fun main() {
    application {
        configure {
            width = 480
            height = 320
        }

        program {
            extend(UI())

            val state = object {
                var left = 3.0
                var right = 6.0
                var range: ClosedRange<Double> = 2.0..8.0

                var ileft = 0
                var iright = 8
                var irange: ClosedRange<Int> = 0..5
            }

            widget(drawer.bounds.offsetEdges(-5.0)) {
                gridSlots(2, 2, gutterX = 10.0, gutterY = 10.0) { rects ->
                    rangeSlider(rects[0], state::left, state::right, 0.0..12.0)
                    rangeSlider(rects[1], state::range, 0.0..12.0)
                    rangeSlider(rects[2], state::ileft, state::iright, 2..15)
                    rangeSlider(rects[3], state::irange, 0..12)
                }
            }

        }
    }
}