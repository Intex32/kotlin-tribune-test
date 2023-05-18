package tribune

import arrow.core.Nel
import com.sksamuel.tribune.core.Parser
import kotlin.reflect.KProperty1

/**
 * [Parser] where [E] is wrapped by [ParseError].
 * This is the base type for all functionality in this file
 * as it requires the error [E] to be of type [ParseError].
 */
typealias EParser<I, A, E> = Parser<I, A, ParseError<E>>

/**
 * base error type for [EParser]
 */
sealed interface ParseError<out T> {
    fun toDisplayString(): String =
        StringBuilder().also(::buildDisplayString).toString()

    fun buildDisplayString(builder: StringBuilder): Unit = when (this@ParseError) {
        is TerminalParseError -> builder.append(error.toString())
        is FocussedError -> {
            builder.append("$field: ")
            error.buildDisplayString(builder)
        }
//        is PotentialCaseError -> {
//            builder.append("{ ")
//            subErrors.forEach { error ->
//                error.buildDisplayString(builder)
//                builder.append("; ")
//            }
//            builder.append("}")
//        }
    }.unit()
}

/**
 * An error that only holds an actual error [T].
 * Instances of this error type are always the end
 * of a top-down error graph. Therefore, they
 * terminate each branch.
 */
@JvmInline
value class TerminalParseError<T>(
    val error: T,
) : ParseError<T> {
    companion object {
        fun <T> wrapEach(errors: Nel<T>): Nel<ParseError<T>> =
            errors.map { TerminalParseError(it) }
    }
}

/**
 * Focuses the sub error [error] to hint
 * what more specific item/entity the [error] is caused by.
 * [field] is usually the name of a property.
 */
data class FocussedError<T>(
    val field: String, //TODO: find better, more abstract name
    val error: ParseError<T>,
//    val case: String? = null,
) : ParseError<T>

//data class PotentialCaseError<T>(
//    val subErrors: Nel<ParseError<T>>,
//) : ParseError<T>

/**
 * Widens the input from [I] to [I2].
 * This mapping is done through [iProp].
 */
infix fun <I, A, E, I2> Parser<I, A, ParseError<E>>.widenByProp(iProp: KProperty1<I2, I>): Parser<I2, A, ParseError<E>> = Parser { x ->
    parse(iProp.get(x))
        .mapLeft { errors ->
            errors.map { error ->
                FocussedError(field = iProp.name, error = error)
            }
        }
}

/**
 * @see widenByProp
 */
@JvmName("wrapAndWidenByProp")
infix fun <I, A, E, I2> Parser<I, A, E>.widenByProp(i: KProperty1<I2, I>): Parser<I2, A, ParseError<E>> =
    wrapTerminalError()
        .widenByProp(i)

/**
 * @see widenByProp
 */
fun <I, E, I2> Parser.Companion.fromAndWidenByProp(i: KProperty1<I2, I>): Parser<I2, I, ParseError<E>> =
    from<I>().widenByProp(i)

/**
 * Wraps all errors in [FocussedError].
 * [FocussedError.field] corresponds to the properties' names specified.
 * @see FocussedError
 */
fun <I, A, E> Parser<I, A, ParseError<E>>.focusErrorsByProps(
    property0: KProperty1<I, *>,
    vararg properties: KProperty1<I, *>
): Parser<I, A, FocussedError<E>> = Parser { x ->
    val propertiesNel = Nel(property0, properties.toList())
    parse(x)
        .mapLeft { errors ->
            errors.flatMap { error ->
                propertiesNel.map { property ->
                    FocussedError(property.name, error)
                }
            }
        }
}

/**
 * converts [Parser] to [EParser]
 * @see EParser
 */
fun <I, A, E> Parser<I, A, E>.wrapTerminalError(): EParser<I, A, E> = Parser { x ->
    parse(x)
        .mapLeft { errors ->
            errors.map { error ->
                TerminalParseError(error)
            }
        }
}