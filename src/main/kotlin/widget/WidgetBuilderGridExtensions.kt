package widget

import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import org.openrndr.shape.Rectangle

fun WidgetBuilder.grid(
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

fun WidgetBuilder.grid(
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

fun <T> WidgetBuilder.gridIndexed(
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


fun <T> WidgetBuilder.gridIndexed(
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

fun WidgetBuilder.irregularGrid(
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

fun <T> WidgetBuilder.irregularGridIndexed(
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
