package widget

import org.openrndr.extra.shapes.primitives.grid
import org.openrndr.extra.shapes.primitives.irregularGrid
import org.openrndr.shape.Rectangle

fun <T> Widget.grid(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: Widget.(Rectangle) -> T
): List<T> {
    return bounds
        .grid(columns, rows, marginX, marginY, gutterX, gutterY)
        .flatten()
        .map { element(it) }
}

fun <T> Widget.gridIndexed(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: Widget.(Int, Rectangle) -> T
): List<T> {
    return bounds
        .grid(columns, rows, marginX, marginY, gutterX, gutterY)
        .flatten()
        .mapIndexed { i, r -> element(i, r) }
}

fun Widget.gridSlots(
    columns: Int,
    rows: Int,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    elements: Widget.(List<Rectangle>) -> Unit
) {
    elements(
        bounds
            .grid(columns, rows, marginX, marginY, gutterX, gutterY)
            .flatten()
    )
}

fun <T> Widget.grid(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: Widget.(Rectangle) -> T
): List<T> {
    return bounds
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

fun <T> Widget.gridIndexed(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    element: Widget.(Int, Rectangle) -> T
): List<T> {
    return bounds
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

fun Widget.gridSlots(
    cellWidth: Double,
    cellHeight: Double,
    minMarginX: Double = 0.0,
    minMarginY: Double = 0.0,
    gutterX: Double = 0.0,
    gutterY: Double = 0.0,
    elements: Widget.(List<Rectangle>) -> Unit
) {
    elements(
        bounds
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

fun <T> Widget.irregularGrid(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: Widget.(Rectangle) -> T
): List<T> {
    return bounds
        .irregularGrid(
            columnWeights,
            rowWeights,
            marginX,
            marginY
        )
        .flatten()
        .map { element(it) }
}

fun <T> Widget.irregularGridIndexed(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    element: Widget.(Int, Rectangle) -> T
): List<T> {
    return bounds
        .irregularGrid(
            columnWeights,
            rowWeights,
            marginX,
            marginY
        )
        .flatten()
        .mapIndexed { i, r -> element(i, r) }
}

fun Widget.irregularGridSlots(
    columnWeights: List<Double>,
    rowWeights: List<Double>,
    marginX: Double = 0.0,
    marginY: Double = 0.0,
    elements: Widget.(List<Rectangle>) -> Unit
) {
    elements(
        bounds
            .irregularGrid(
                columnWeights,
                rowWeights,
                marginX,
                marginY
            )
            .flatten()
    )
}