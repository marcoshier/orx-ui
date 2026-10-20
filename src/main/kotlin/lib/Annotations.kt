package lib

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScaleParameter(
    val label: String,
    val minX: Double,
    val maxX: Double,
    val minY: Double,
    val maxY: Double,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class RotationParameter(
    val label: String,
    val min: Double,
    val max: Double,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Matrix44Parameter(
    val label: String,
    val minTranslateX: Double,
    val maxTranslateX: Double,
    val minTranslateY: Double,
    val maxTranslateY: Double,
    val minScale: Double,
    val maxScale: Double,
    val minAngle: Double,
    val maxAngle: Double,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntVector2Parameter(
    val label: String,
    val min: Int,
    val max: Int,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntVector3Parameter(
    val label: String,
    val min: Int,
    val max: Int,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntVector4Parameter(
    val label: String,
    val min: Int,
    val max: Int,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntRangeParameter(
    val label: String,
    val min: Int,
    val max: Int,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class DoubleRangeParameter(
    val label: String,
    val min: Double,
    val max: Double,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextboxParameter(
    val label: String,
    val order: Int = Int.MAX_VALUE
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ContourParameter(
    val label: String,
    val order: Int = Int.MAX_VALUE,
    val precision: Int = 1
)