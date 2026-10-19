package style

import org.openrndr.color.ColorRGBa

object StyleLibrary {
    val DEFAULT = Style().apply {
        marginX = 0.0
        marginY = 0.0
        paddingX = 0.0
        paddingY = 0.0
        minWidth = 10.0
        maxWidth = Double.MAX_VALUE
        minHeight = 10.0
        maxHeight = Double.MAX_VALUE
        fontFamily = Font()
        fontSize = 16.0
        textColor = ColorRGBa.BLACK
        backgroundColor = Colors.CLICKABLE
        hoverColor = Colors.HOVERED
        focusColor = Colors.FOCUSED
        selectColor = Colors.SELECTED
        strokeColor = ColorRGBa.BLACK
        strokeWeight = 0.0
        borderRadius = 0.0
    }


    fun fromUrl(): Style = Style() // todo
}