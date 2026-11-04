import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.shape.Rectangle
import ui.UIElement
import widget.Direction
import widget.Widget
import widget.WidgetBuilder
import widget.widget

fun main() {
    application {
        program {
            extend(UI())

            class ExampleWidget(bounds: Rectangle): Widget(bounds) {
                override fun draw(drawer: Drawer, focusElement: UIElement?) {
                    style.backgroundColor = ColorRGBa.GREEN
                    super.draw(drawer, focusElement)
                }
            }

            fun WidgetBuilder.exampleWidget(): ExampleWidget {
                val e =  ExampleWidget(currentElementBounds)
                e.parent = this.widget
                add()
                return e
            }

            widget(drawer.bounds.offsetEdges(-10.0)) {
                repeat(5) {
                    minHeight = 30.0
                    maxHeight = 30.0
                    this.elementWidth = bounds.width - 10.0
                    this.elementHeight = 30.0
                    println(currentElementBounds)
                    append(Direction.DOWN) {
                        exampleWidget()
                    }
                }
            }

            extend {


            }
        }
    }
}