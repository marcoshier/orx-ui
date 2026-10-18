package style

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.openrndr.animatable.easing.Easing
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
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

    var fontFamily: Font = Font()
    var fontSize: Double = 16.0
    var textColor: ColorRGBa? = Colors.BLACK

    var backgroundColor: ColorRGBa? = Colors.CLICKABLE
    var hoverColor: ColorRGBa? = Colors.HOVERED
    var focusColor: ColorRGBa? = Colors.FOCUSED
    var selectColor: ColorRGBa? = Colors.SELECTED
    var strokeColor: ColorRGBa? = Colors.BLACK

    var strokeWeight: Double = 0.0
    var borderRadius: Double = 0.0

    var transitions: MutableMap<String, TransitionDescriptor> = mutableMapOf()

    val font by lazy {
        val context = UIHost.current.context
        val fonts = context.fonts
        val program = context.program

        val size = (fontSize * 2).roundToInt() / 2.0 // this could be a user-settable thing
        val key = "$fontFamily-$size"
        val file = File(fontFamily.path, "${fontFamily.name}.${fontFamily.format}")

        fonts.getOrPut(key) {
            program.loadFont(file.invariantSeparatorsPath, size)
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

        s.fontFamily = fontFamily
        s.fontSize = fontSize
        s.textColor = textColor

        s.backgroundColor = backgroundColor
        s.hoverColor = hoverColor
        s.focusColor = focusColor
        s.selectColor = selectColor
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
                "fontFamily=$fontFamily, " +
                "fontSize=$fontSize, " +
                "textColor=$textColor, " +
                "backgroundColor=$backgroundColor, " +
                "hoverColor=$hoverColor, " +
                "focusColor=$focusColor, " +
                "selectColor=$selectColor, " +
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
        .filter { it.name != "transitions" && it.name != "definedProperties" }
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
