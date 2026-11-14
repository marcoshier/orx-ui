package style

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.openrndr.animatable.easing.Easing
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.MinifyingFilter
import org.openrndr.draw.loadFont
import ui.UIHost
import ui.window
import java.io.File
import kotlin.math.roundToInt
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0
import kotlin.reflect.full.memberProperties

@Serializable
data class Style(val name: String = "") {

    var marginX: Double = 0.0
    var marginY: Double = 0.0
    var paddingX: Double = 0.0
    var paddingY: Double = 0.0
    var minWidth: Double = 0.0
    var maxWidth: Double = Double.MAX_VALUE
    var minHeight: Double = 0.0
    var maxHeight: Double = Double.MAX_VALUE

    var font: Font = Font()
    var fontSize: Double = 16.0

    var textColor: ColorRGBa? = Colors.BLACK
    var backgroundColor: ColorRGBa? = Colors.CLICKABLE
    var strokeColor: ColorRGBa? = null

    var checkboxBackgroundFill: ColorRGBa? = Colors.CLICKABLE.shade(0.8)
    var checkboxStroke: ColorRGBa? = null

    var dropdownEntryFill: ColorRGBa? = Colors.CLICKABLE

    var sliderInactiveRailFill: ColorRGBa? = Colors.CLICKABLE.shade(0.875)
    var sliderActiveRailFill: ColorRGBa? = Colors.CLICKABLE.shade(0.575)
    var sliderRailStroke: ColorRGBa? = null
    var sliderHandleFill: ColorRGBa? = null
    var sliderHandleStroke: ColorRGBa? = Colors.SELECTED.shade(0.5)
    var sliderActiveTextColor: ColorRGBa? = textColor

    var rotateSliderStroke: ColorRGBa? = Colors.CLICKABLE.shade(0.5)
    var rotateSliderFill: ColorRGBa? = null

    var scaleControlLinesStroke: ColorRGBa? = Colors.CLICKABLE.shade(0.65)
    var scaleControlActiveBoxFill: ColorRGBa? = Colors.CLICKABLE.shade(0.65)

    var textBaselineColor: ColorRGBa? = Colors.CLICKABLE.shade(0.65)
    var textboxHandleColor: ColorRGBa? = Colors.CLICKABLE.shade(0.65)
    var textSelectionColor: ColorRGBa? = Colors.CLICKABLE.shade(0.85)

    var xyControlLineStroke: ColorRGBa? = Colors.CLICKABLE.shade(0.65)
    var xyControlHandleFill: ColorRGBa? = Colors.HOVERED.shade(0.65)

    var strokeWeight: Double = 0.0
    var borderRadius: Double = 0.0

    var transitions: MutableMap<String, TransitionDescriptor> = mutableMapOf()

    val fontMap by lazy {
        val context = UIHost.current
        val fonts = context.fonts
        val program = context.program

        val size = (fontSize * context.fontSizeStep).roundToInt() / context.fontSizeStep
        val key = "$font-$size"
        val file = File(font.path, "${font.name}.${font.format}")

        fonts.getOrPut(key) {
            program.loadFont(file.invariantSeparatorsPath, size, contentScale = window().contentScale).also {
                it.texture.filter(MinifyingFilter.NEAREST, MagnifyingFilter.NEAREST)
                it.texture.generateMipmaps()
            }
        }
    }

    @Transient
    var setProperties: Set<String>? = null

    /** Transitions */
    @JvmName("transitionDouble")
    fun transition(prop: KProperty0<Double>, duration: Double = 0.3, ease: Easing = Easing.None, delay: Double = 0.0) {
        transitions[prop.name] = TransitionDescriptor(duration, ease, delay)
    }

    @JvmName("transitionColor")
    fun transition(prop: KProperty0<ColorRGBa?>, duration: Double = 0.3, ease: Easing = Easing.None, delay: Double = 0.0) {
        transitions[prop.name] = TransitionDescriptor(duration, ease, delay)
    }

    @JvmName("transitionAll")
    fun transition(duration: Double = 0.3, ease: Easing = Easing.None, delay: Double = 0.0) {
        for (prop in styleDoubleProperties) {
            transitions[prop.name] = TransitionDescriptor(duration, ease, delay)
        }
        for (prop in styleColorProperties) {
            transitions[prop.name] = TransitionDescriptor(duration, ease, delay)
        }
    }

    operator fun invoke(block: Style.() -> Unit) = apply(block)

    fun apply(style: Style) {
        val defined = style.setProperties

        if (defined != null) {
            for (p in styleProperties) {
                if (p.name in defined) {
                    p.set(this, p.get(style))
                }
            }
        } else {
            val default = Style()
            for (p in styleProperties) {
                val v = p.get(style)
                if (v != p.get(default)) {
                    p.set(this, v)
                }
            }
        }

        transitions.putAll(style.transitions)
    }

    fun copy(): Style {
        val s = Style(name)

        s.marginX = marginX
        s.marginY = marginY
        s.paddingX = paddingX
        s.paddingY = paddingY
        s.minWidth = minWidth
        s.maxWidth = maxWidth
        s.minHeight = minHeight
        s.maxHeight = maxHeight

        s.font = font
        s.fontSize = fontSize
        s.textColor = textColor

        s.backgroundColor = backgroundColor
        s.strokeColor = strokeColor

        s.strokeWeight = strokeWeight
        s.borderRadius = borderRadius

        s.transitions = transitions.toMutableMap()
        s.setProperties = setProperties?.toSet()

        return s
    }

    override fun toString(): String {
        return "Style(" +
                "name='$name', " +
                "marginX=$marginX, " +
                "marginY=$marginY, " +
                "paddingX=$paddingX, " +
                "paddingY=$paddingY, " +
                "minWidth=$minWidth, " +
                "maxWidth=$maxWidth, " +
                "minHeight=$minHeight, " +
                "maxHeight=$maxHeight, " +
                "fontFamily=$font, " +
                "fontSize=$fontSize, " +
                "textColor=$textColor, " +
                "backgroundColor=$backgroundColor, " +
                "strokeColor=$strokeColor, " +
                "strokeWeight=$strokeWeight, " +
                "borderRadius=$borderRadius, " +
                "transitions=$transitions" +
                ")"
    }
}

@Suppress("UNCHECKED_CAST")
val styleProperties: List<KMutableProperty1<Style, Any?>> by lazy {
    Style::class.memberProperties
        .filterIsInstance<KMutableProperty1<Style, Any?>>()
        .filter { it.name != "transitions" && it.name != "setProperties" }
}

@Suppress("UNCHECKED_CAST")
val styleDoubleProperties: List<KMutableProperty1<Style, Double>> by lazy {
    Style::class.memberProperties
        .filterIsInstance<KMutableProperty1<Style, *>>()
        .filter { it.returnType.classifier == Double::class }
        .map { it as KMutableProperty1<Style, Double> }
}

@Suppress("UNCHECKED_CAST")
val styleColorProperties: List<KMutableProperty1<Style, ColorRGBa?>> by lazy {
    Style::class.memberProperties
        .filterIsInstance<KMutableProperty1<Style, *>>()
        .filter { it.returnType.classifier == ColorRGBa::class }
        .map { it as KMutableProperty1<Style, ColorRGBa?> }
}