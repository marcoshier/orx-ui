package lib

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigUtil
import com.typesafe.config.ConfigValueFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleListParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.ListParameter
import org.openrndr.extra.parameters.MutableListParameter
import org.openrndr.extra.parameters.OptionParameter
import org.openrndr.extra.parameters.PathParameter
import org.openrndr.extra.parameters.TextParameter
import org.openrndr.extra.parameters.Vector2Parameter
import org.openrndr.extra.parameters.Vector3Parameter
import org.openrndr.extra.parameters.Vector4Parameter
import org.openrndr.extra.parameters.XYParameter
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.panel.collections.SelectableList
import org.openrndr.panel.collections.SelectableMutableList
import java.io.File
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/** Lifted from GUI, integrated with new annotations/types */

class XParameter(
    val parameterType: XParameterType,
    val property: KMutableProperty1<out Any, Any?>?,
    val function: KCallable<Unit>?,
    val label: String,
    val doubleRange: ClosedRange<Double>?,
    val vectorRange: Pair<Vector2, Vector2>?,
    val sizeRange: ClosedRange<Int>?,
    val intRange: IntRange?,
    val translateRangeX: ClosedRange<Double>? = null,
    val translateRangeY: ClosedRange<Double>? = null,
    val scaleRange: ClosedRange<Double>? = null,
    val angleRange: ClosedRange<Double>? = null,
    val precision: Int?,
    val invertY: Boolean?,
    val showVector: Boolean?,
//        val optionEnum: Enum<*>,
    val absolutePath: Boolean?,
    val pathContext: String?,
    val pathExtensions: Array<String>?,
    val pathIsDirectory: Boolean?,
    val order: Int,
)

fun XParameterType.Companion.forParameterAnnotationClass(annotation: Annotation): XParameterType =
    XParameterType.entries.find { it.annotationClass == annotation.annotationClass } ?: error("no type for $annotation")

val XParameterType.Companion.parameterAnnotationClasses get() = XParameterType.entries.map { it.annotationClass }

enum class XParameterType(val annotationClass: KClass<out Annotation>) {
    Double(DoubleParameter::class),
    Int(IntParameter::class),
    Boolean(BooleanParameter::class),
    Action(ActionParameter::class),
    Text(TextParameter::class),
    Color(ColorParameter::class),
    XY(XYParameter::class),
    DoubleList(DoubleListParameter::class),
    Vector2(Vector2Parameter::class),
    Vector3(Vector3Parameter::class),
    Vector4(Vector4Parameter::class),
    Option(OptionParameter::class),
    List(ListParameter::class),
    MutableList(MutableListParameter::class),
    Path(PathParameter::class),
    Scale(ScaleParameter::class),
    Rotation(RotationParameter::class),
    Matrix44(Matrix44Parameter::class),
    IntRange(IntRangeParameter::class),
    DoubleRange(DoubleRangeParameter::class),
    Textbox(TextboxParameter::class),
    Contour(ContourParameter::class)
    ;

    companion object {

    }

}



fun Any.listXParameters(): List<XParameter> {
    return (this::class.memberProperties.filter { property ->
        !property.isConst &&
                property is KMutableProperty1<*, *> &&
                property.visibility == KVisibility.PUBLIC &&
                property.annotations.map { it.annotationClass }.intersect(XParameterType.parameterAnnotationClasses.toSet()).isNotEmpty()
    }.map { property ->
        val annotations = property.annotations.filter { it.annotationClass in XParameterType.parameterAnnotationClasses }
        var intRange: IntRange? = null
        var doubleRange: ClosedRange<Double>? = null
        var sizeRange: ClosedRange<Int>? = null
        var order: Int = Int.MAX_VALUE
        var label = ""
        var precision: Int? = null
        var type: XParameterType? = null
        var vectorRange = Pair(Vector2(-1.0, -1.0), Vector2(1.0, 1.0))
        var translateRangeX: ClosedRange<Double>? = null
        var translateRangeY: ClosedRange<Double>? = null
        var scaleRange: ClosedRange<Double>? = null
        var angleRange: ClosedRange<Double>? = null
        var invertY: Boolean? = null
        var showVector: Boolean? = null
        var absolutePath: Boolean? = null
        var pathContext: String? = null
        var pathExtensions: Array<String>? = null
        var pathIsDirectory: Boolean? = null

        for (it in annotations) {
            type = XParameterType.forParameterAnnotationClass(it)
            when (it) {
                is BooleanParameter -> {
                    label = it.label
                    order = it.order
                }
                is DoubleParameter -> {
                    label = it.label
                    doubleRange = it.low..it.high
                    precision = it.precision
                    order = it.order
                }
                is IntParameter -> {
                    label = it.label
                    intRange = it.low..it.high
                    order = it.order
                }
                is TextParameter -> {
                    label = it.label
                    order = it.order
                }
                is ColorParameter -> {
                    label = it.label
                    order = it.order
                }
                is XYParameter -> {
                    label = it.label
                    order = it.order
                    vectorRange = Pair(Vector2(it.minX, it.minY), Vector2(it.maxX, it.maxY))
                    precision = it.precision
                    invertY = it.invertY
                    showVector = it.showVector
                }
                is DoubleListParameter -> {
                    label = it.label
                    order = it.order
                    doubleRange = it.low..it.high
                    precision = it.precision
                    sizeRange = it.minimumListLength..it.maximumListLength
                }
                is Vector2Parameter -> {
                    label = it.label
                    order = it.order
                    doubleRange = it.min..it.max
                    precision = it.precision
                }
                is Vector3Parameter -> {
                    label = it.label
                    order = it.order
                    doubleRange = it.min..it.max
                    precision = it.precision
                }
                is Vector4Parameter -> {
                    label = it.label
                    order = it.order
                    doubleRange = it.min..it.max
                    precision = it.precision
                }
                is OptionParameter -> {
                    label = it.label
                    order = it.order
                }
                is PathParameter -> {
                    label = it.label
                    absolutePath =  it.absolute
                    pathContext = it.context
                    pathExtensions = it.extensions
                    pathIsDirectory = it.directory
                }
                is ListParameter -> {
                    label = it.label
                    order = it.order
                }
                is MutableListParameter -> {
                    label = it.label
                    order = it.order
                }
                is ScaleParameter -> {
                    label = it.label
                    if (it.minX == it.minY && it.maxX == it.maxY) {
                        doubleRange = it.minX..it.maxX
                    } else {
                        vectorRange = Pair(Vector2(it.minX, it.minY), Vector2(it.maxX, it.maxY))
                    }
                    precision = it.precision
                    order = it.order
                }
                is RotationParameter -> {
                    label = it.label
                    doubleRange = it.min..it.max
                    precision = it.precision
                    order = it.order
                }
                is Matrix44Parameter -> {
                    label = it.label
                    translateRangeX = it.minTranslateX..it.maxTranslateX
                    translateRangeY = it.minTranslateY..it.maxTranslateY
                    scaleRange = it.minScale..it.maxScale
                    angleRange = it.minAngle..it.maxAngle
                    order = it.order
                    precision = it.precision
                }
                is IntRangeParameter -> {
                    label = it.label
                    intRange = it.min..it.max
                    order = it.order
                }
                is DoubleRangeParameter -> {
                    label = it.label
                    doubleRange = it.min..it.max
                    order = it.order
                    precision = it.precision
                }
                is TextboxParameter -> {
                    label = it.label
                    order = it.order
                }
                is ContourParameter -> {
                    label = it.label
                    order = it.order
                    precision = it.precision
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        XParameter(
            parameterType = type ?: error("no type"),
            property = property as KMutableProperty1<out Any, Any?>,
            function = null,
            label = label,
            doubleRange = doubleRange,
            vectorRange = vectorRange,
            sizeRange = sizeRange,
            intRange = intRange,
            precision = precision,
            showVector = showVector,
            invertY = invertY,
            absolutePath = absolutePath,
            pathContext =  pathContext,
            pathExtensions = pathExtensions,
            pathIsDirectory = pathIsDirectory,
            order = order,

            )
    } + this::class.declaredMemberFunctions.filter {
        it.findAnnotation<ActionParameter>() != null
    }.map {
        val annotation = it.findAnnotation<ActionParameter>()!!
        val label = annotation.label
        val order = annotation.order
        val type = XParameterType.Action
        @Suppress("UNCHECKED_CAST")
        XParameter(
            type,
            property = null,
            function = it as KCallable<Unit>,
            label = label,
            doubleRange = null,
            intRange = null,
            sizeRange = null,
            vectorRange = null,
            precision = null,
            showVector = null,
            invertY = null,
            absolutePath = null,
            pathContext = null,
            pathExtensions = null,
            pathIsDirectory = null,
            order = order
        )
    }
            ).sortedBy { it.order }
}

private val logger = KotlinLogging.logger {  }

class ParameterStore(private val file: File) {
    private var config: Config =
        if (file.exists()) ConfigFactory.parseFile(file) else ConfigFactory.empty()


    init {
        logger.info { "Initialized parameter store ${file.path}" }
    }

    fun load(key: String, obj: Any, params: List<XParameter>) {
        if (!config.hasPath(key)) return

        val section = config.getConfig(key)
        for (p in params) {
            val name = p.property?.name ?: continue
            if (!section.hasPath(name)) continue

            try {
                decode(p, obj, section, name)
            } catch (e: Throwable) {
                logger.warn { "skipping ${p.label}: ${e.message}" }
            }
        }
    }

    fun save(key: String, obj: Any, params: List<XParameter>) {
        val values = params.mapNotNull { p ->
            val name = p.property?.name ?: return@mapNotNull null
            val encoded = encode(p, obj)

            name to encoded
        }.toMap()

        config = config.withValue(ConfigUtil.quoteString(key), ConfigValueFactory.fromMap(values))
        file.parentFile?.mkdirs()
        file.writeText(config.root().render(hoconConfigOptions))

        logger.debug { "Parameters saved." }
    }

    @Suppress("UNCHECKED_CAST")
    private fun encode(p: XParameter, obj: Any): Any? {
        val v = (p.property as KMutableProperty1<Any, Any?>).get(obj) ?: return null
        return when (v) {
            is Double, is Int, is Boolean, is String -> v
            is Enum<*> -> v.name
            is SelectableList<*> -> v.selectedIndex
            is SelectableMutableList<*> -> v.selectedIndex
            is Vector2 -> listOf(v.x, v.y)
            is Vector3 -> listOf(v.x, v.y, v.z)
            is Vector4 -> listOf(v.x, v.y, v.z, v.w)
            is Matrix44 -> v.toDoubleArray().toList()
            is ColorRGBa -> listOf(v.r, v.g, v.b, v.alpha)
            is ClosedRange<*> -> listOf(v.start, v.endInclusive)
            else -> null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun decode(p: XParameter, obj: Any, c: Config, k: String) {
        val prop = p.property as KMutableProperty1<Any, Any?>
        val current = prop.get(obj)
        val value: Any? = when (current) {
            is Double -> c.getDouble(k).let { d -> p.doubleRange?.let { d.coerceIn(it) } ?: d }
            is Int -> c.getInt(k).let { i -> p.intRange?.let { i.coerceIn(it) } ?: i }
            is Boolean -> c.getBoolean(k)
            is String -> c.getString(k)
            is Enum<*> -> current.javaClass.enumConstants.first { (it as Enum<*>).name == c.getString(k) }
            is SelectableList<*> -> { current.selectedIndex = c.getInt(k).coerceIn(-1, current.lastIndex); return }
            is SelectableMutableList<*> -> { current.selectedIndex = c.getInt(k).coerceIn(-1, current.lastIndex); return }
            is Vector2 -> c.getDoubleList(k).let { Vector2(it[0], it[1]) }
            is Vector3 -> c.getDoubleList(k).let { Vector3(it[0], it[1], it[2]) }
            is Vector4 -> c.getDoubleList(k).let { Vector4(it[0], it[1], it[2], it[3]) }
            is Matrix44 -> Matrix44.fromDoubleArray(c.getDoubleList(k).toDoubleArray())
            is ColorRGBa -> c.getDoubleList(k).let { ColorRGBa(it[0], it[1], it[2], it[3]) }
            is ClosedRange<*> -> if (current.start is Int)
                c.getIntList(k).let { it[0]..it[1] } else c.getDoubleList(k).let { it[0]..it[1] }
            else -> return
        }
        prop.set(obj, value)
    }
}