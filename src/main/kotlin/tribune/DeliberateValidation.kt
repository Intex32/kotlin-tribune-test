package tribune

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.map

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
@RequiresOptIn("This class should only be instantiated by parsers.")
annotation class DeliberatelyValidated

/**
 * Marks [T] as validated by a [Parser].
 *
 * This wrapper is useful if you don't want to create an extra type
 * for every type that you parse.
 *
 * @see declareValidated
 */
data class ValidatedByParser<T> @DeliberatelyValidated internal constructor(
    val value: T,
)

/**
 * Marks [A] as validated by the [Parser].
 *
 * It is NOT strictly enforced by the type system that
 * you can only pass a valid type, but the developer is made
 * aware of this responsibility through having to opt-in with [DeliberatelyValidated].
 * Thus, it is assured that no developer can create a new instance of [ValidatedByParser]
 * without the compiler asking if you really know what you are doing.
 *
 * @see [ValidatedByParser]
 */
@DeliberatelyValidated
fun <I, A, E> Parser<I, A, E>.declareValidated(): Parser<I, ValidatedByParser<A>, E> =
    map { ValidatedByParser(it) }