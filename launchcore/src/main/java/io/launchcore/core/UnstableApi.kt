package io.launchcore.core

/**
 * Marks APIs that are still evolving. During 0.x, everything annotated with this
 * may change or break between releases without deprecation cycles.
 *
 * Downstream code must explicitly opt in:
 * `@OptIn(UnstableApi::class)` or the compiler flag
 * `-opt-in=io.launchcore.core.UnstableApi`.
 */
@RequiresOptIn(message = "LaunchCore 0.x API is unstable and may change without notice before 1.0.")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.TYPEALIAS,
)
annotation class UnstableApi
