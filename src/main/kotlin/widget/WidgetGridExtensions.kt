package widget

import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import org.openrndr.shape.Rectangle
import ui.UIElement

fun Widget.grid(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
): MutableList<UIElement> {
    val builder = WidgetBuilder(bounds, zIndex, parent)
    val slots = bounds.grid(columns, rows, marginX, marginY, gutterX, gutterY).flatten()
    builder.elements(slots)
    for (child in builder.widget.elements) {
        child.parent = this
    }
    return builder.widget.elements
}

fun Widget.grid(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
): MutableList<UIElement> {
    val builder = WidgetBuilder(bounds, zIndex, parent)
    val slots = bounds.grid(cellWidth, cellHeight, minMarginX, minMarginY, gutterX, gutterY).flatten()
    builder.elements(slots)
    for (child in builder.widget.elements) {
        child.parent = this
    }
    return builder.widget.elements
}

fun <T> Widget.gridIndexed(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> T
): List<T> {
    val builder = WidgetBuilder(bounds, zIndex, parent)
    val slots = bounds.grid(columns, rows, marginX, marginY, gutterX, gutterY).flatten()
    val items = slots.mapIndexed { i, r -> builder.element(i, r) }
    for (child in builder.widget.elements) {
        child.parent = this
    }
    for (item in items) {
        if (item is UIElement) item.parent = this
    }
    return items
}

fun <T> Widget.gridIndexed(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> T
): List<T> {
    val builder = WidgetBuilder(bounds, zIndex, parent)
    val slots = bounds.grid(cellWidth, cellHeight, minMarginX, minMarginY, gutterX, gutterY).flatten()
    val items = slots.mapIndexed { i, r -> builder.element(i, r) }
    for (child in builder.widget.elements) {
        child.parent = this
    }
    for (item in items) {
        if (item is UIElement) item.parent = this
    }
    return items
}

fun Widget.irregularGrid(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
): MutableList<UIElement> {
    val builder = WidgetBuilder(bounds, zIndex, parent)
    val slots = bounds.irregularGrid(columnWeights, rowWeights, marginX, marginY).flatten()
    builder.elements(slots)
    for (child in builder.widget.elements) {
        child.parent = this
    }
    return builder.widget.elements
}

fun <T> Widget.irregularGridIndexed(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> T
): List<T> {
    val builder = WidgetBuilder(bounds, zIndex, parent)
    val slots = bounds.irregularGrid(columnWeights, rowWeights, marginX, marginY).flatten()
    val items = slots.mapIndexed { i, r -> builder.element(i, r) }
    for (child in builder.widget.elements) {
        child.parent = this
    }
    for (item in items) {
        if (item is UIElement) item.parent = this
    }
    return items
}