package widgets

import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import org.openrndr.shape.Rectangle
import ui.UIElement

fun WidgetBuilder.grid(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Rectangle) -> UIElement
): List<UIElement> {
    val grid = widget.bounds.grid(
        columns, rows, marginX, marginY, gutterX, gutterY
    ).flatten()

    val gridElements = mutableListOf<UIElement>()

    for (r in grid) {
        val el = this.element(r)
        gridElements.add(el)
        add(el)
    }

    return gridElements
}

fun WidgetBuilder.gridSlots(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
) {
    val grid = widget.bounds.grid(
        columns, rows, marginX, marginY, gutterX, gutterY
    ).flatten()

    elements(grid)
}

fun WidgetBuilder.grid(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> UIElement
): List<UIElement> {
    val grid = widget.bounds.grid(
        columns, rows, marginX, marginY, gutterX, gutterY
    ).flatten()

    val gridElements = mutableListOf<UIElement>()

    for ((i, r) in grid.withIndex()) {
        val el = this.element(i, r)
        gridElements.add(el)
    }

    return gridElements
}

fun WidgetBuilder.grid(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Rectangle) -> UIElement
): List<UIElement> {
    val grid = widget.bounds.grid(
        cellWidth,
        cellHeight,
        minMarginX,
        minMarginY,
        gutterX,
        gutterY
    ).flatten()

    val gridElements = mutableListOf<UIElement>()

    for (r in grid) {
        val el = this.element(r)
        gridElements.add(el)
        add(el)
    }

    return gridElements
}

fun WidgetBuilder.gridSlots(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
) {
    val grid = widget.bounds.grid(
        cellWidth,
        cellHeight,
        minMarginX,
        minMarginY,
        gutterX,
        gutterY
    ).flatten()

    elements(grid)
}

fun WidgetBuilder.grid(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> UIElement
): List<UIElement> {
    val grid = widget.bounds.grid(
        cellWidth,
        cellHeight,
        minMarginX,
        minMarginY,
        gutterX,
        gutterY
    ).flatten()

    val gridElements = mutableListOf<UIElement>()

    for ((i, r) in grid.withIndex()) {
        val el = this.element(i, r)
        gridElements.add(el)
        add(el)
    }

    return gridElements
}

fun WidgetBuilder.irregularGrid(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: WidgetBuilder.(Rectangle) -> UIElement
): List<UIElement> {
    val grid = widget.bounds.irregularGrid(
        columnWeights,
        rowWeights,
        marginX,
        marginY
    ).flatten()

    val gridElements = mutableListOf<UIElement>()

    for (r in grid) {
        val el = this.element(r)
        gridElements.add(el)
        add(el)
    }

    return gridElements
}

fun WidgetBuilder.irregularGridSlots(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
) {
    val grid = widget.bounds.irregularGrid(
        columnWeights,
        rowWeights,
        marginX,
        marginY
    ).flatten()

    elements(grid)
}

fun WidgetBuilder.irregularGrid(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> UIElement
): List<UIElement> {
    val grid = widget.bounds.irregularGrid(
        columnWeights,
        rowWeights,
        marginX,
        marginY
    ).flatten()

    val gridElements = mutableListOf<UIElement>()

    for ((i, r) in grid.withIndex()) {
        val el = this.element(i, r)
        gridElements.add(el)
        add(el)
    }

    return gridElements
}