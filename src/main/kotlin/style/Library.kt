package style

import org.openrndr.color.ColorRGBa

object StyleLibrary {
    val DEFAULT = buildStylesheet {
        "*" {
            marginX = 0.0
            marginY = 0.0
            paddingX = 0.0
            paddingY = 0.0
            minWidth = 10.0
            maxWidth = Double.MAX_VALUE
            minHeight = 10.0
            maxHeight = Double.MAX_VALUE
            font = Font()
            fontSize = 18.0
            textColor = ColorRGBa.BLACK
            backgroundColor = Colors.CLICKABLE
            strokeColor = null
            strokeWeight = 0.0
            borderRadius = 2.0
        }
        "*"(hover) { backgroundColor = Colors.HOVERED }
        "*"(active) { backgroundColor = Colors.SELECTED }
        "*"(selected) { backgroundColor = Colors.SELECTED }
        "*"(hover, selected) { backgroundColor = Colors.SELECTED.shade(0.75) }

        "checkbox" {
            backgroundColor = Colors.CLICKABLE
            checkboxBackgroundFill = Colors.CLICKABLE
        }
        "checkbox"(hover) {
            backgroundColor = Colors.HOVERED
            checkboxBackgroundFill = ColorRGBa.TRANSPARENT
        }
        "checkbox"(hover, selected) {
            checkboxBackgroundFill = Colors.SELECTED
        }
        "checkbox"(selected) {
            checkboxBackgroundFill = Colors.SELECTED
        }

        "dropdown"(hover) {
            dropdownEntryFill = Colors.HOVERED.shade(0.75)
        }

        "slider"(hover) {
            backgroundColor = Colors.CLICKABLE
            sliderInactiveRailFill = Colors.HOVERED.shade(0.95)
        }

        "rotatecontrol"(hover) {
            backgroundColor = Colors.CLICKABLE.shade(0.95)
            rotateSliderStroke = Colors.HOVERED.shade(0.5)
        }

        "rotatecontrol"(active) {
            rotateSliderStroke = Colors.SELECTED.shade(0.5)
        }

        "scalecontrol"(hover) {
            backgroundColor = Colors.CLICKABLE.shade(0.95)
        }

        "scalecontrol"(active) {
            scaleControlActiveBoxFill = Colors.SELECTED
        }

        "xycontrol"(active) {
            backgroundColor = Colors.HOVERED.shade(0.75)
            xyControlHandleFill = Colors.SELECTED.shade(0.5)
        }
        "xycontrol"(selected) {
            backgroundColor = Colors.HOVERED.shade(0.75)
            xyControlHandleFill = Colors.SELECTED.shade(0.5)
        }

        "textinput"(selected) {
            textBaselineColor = Colors.SELECTED
        }

        "textinput"(active) {
            textBaselineColor = Colors.SELECTED
        }

        "panellabel" {
            font = Font("IBMPlexSans-SemiBold", "ttf")
            fontSize = 16.0
            textColor = Colors.SELECTED.shade(1.5)
        }

    }


    fun fromUrl(): Style = Style() // todo
}