package widgets

import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import org.openrndr.shape.Rectangle

fun <T> WidgetBuilder.grid(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Rectangle) -> T
): List<T> {
    return widget.bounds
        .grid(columns, rows, marginX, marginY, gutterX, gutterY)
        .flatten()
        .map { element(it) }
}

fun <T> WidgetBuilder.grid(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> T
): List<T> {
    return widget.bounds
        .grid(columns, rows, marginX, marginY, gutterX, gutterY)
        .flatten()
        .mapIndexed { i, r -> element(i, r) }
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
    elements(
        widget.bounds
            .grid(columns, rows, marginX, marginY, gutterX, gutterY)
            .flatten()
    )
}

fun <T> WidgetBuilder.grid(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Rectangle) -> T
): List<T> {
    return widget.bounds
        .grid(
            cellWidth,
            cellHeight,
            minMarginX,
            minMarginY,
            gutterX,
            gutterY
        )
        .flatten()
        .map { element(it) }
}

fun <T> WidgetBuilder.grid(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> T
): List<T> {
    return widget.bounds
        .grid(
            cellWidth,
            cellHeight,
            minMarginX,
            minMarginY,
            gutterX,
            gutterY
        )
        .flatten()
        .mapIndexed { i, r -> element(i, r) }
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
    elements(
        widget.bounds
            .grid(
                cellWidth,
                cellHeight,
                minMarginX,
                minMarginY,
                gutterX,
                gutterY
            )
            .flatten()
    )
}

fun <T> WidgetBuilder.irregularGrid(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: WidgetBuilder.(Rectangle) -> T
): List<T> {
    return widget.bounds
        .irregularGrid(
            columnWeights,
            rowWeights,
            marginX,
            marginY
        )
        .flatten()
        .map { element(it) }
}

fun <T> WidgetBuilder.irregularGrid(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: WidgetBuilder.(Int, Rectangle) -> T
): List<T> {
    return widget.bounds
        .irregularGrid(
            columnWeights,
            rowWeights,
            marginX,
            marginY
        )
        .flatten()
        .mapIndexed { i, r -> element(i, r) }
}

fun WidgetBuilder.irregularGridSlots(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    elements: WidgetBuilder.(List<Rectangle>) -> Unit
) {
    elements(
        widget.bounds
            .irregularGrid(
                columnWeights,
                rowWeights,
                marginX,
                marginY
            )
            .flatten()
    )
}