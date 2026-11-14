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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
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
    internal val extras = mutableMapOf<String, Any?>()

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

        extras.putAll(style.extras)
        transitions.putAll(style.transitions)
    }

    fun copy(): Style {
        val s = Style(name)
        for (p in styleProperties) p.set(s, p.get(this))
        s.transitions = transitions.toMutableMap()
        s.setProperties = setProperties?.toSet()
        s.extras.putAll(extras)
        return s
    }

    override fun toString(): String {
        val props = styleProperties.joinToString(", ") { "${it.name}=${it.get(this)}" }
        val ext = extras.entries.joinToString(", ") { "${it.key}=${it.value}" }
        return buildString {
            append("Style(name='$name', $props")
            if (ext.isNotEmpty()) append(", $ext")
            append(", transitions=$transitions)")
        }
    }
}

fun <T> styleProperty(default: T) = object : ReadWriteProperty<Style, T> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Style, property: KProperty<*>): T =
        if (property.name in thisRef.extras) thisRef.extras[property.name] as T else default

    override fun setValue(thisRef: Style, property: KProperty<*>, value: T) {
        thisRef.extras[property.name] = value
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