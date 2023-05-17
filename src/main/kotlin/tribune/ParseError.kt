package tribune

import arrow.core.Nel
import com.sksamuel.tribune.core.Parser
import kotlin.reflect.KProperty1

typealias EParser<I, A, E> = Parser<I, A, ParseError<E>>

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

@JvmInline
value class TerminalParseError<T>(
    val error: T,
) : ParseError<T> {
    companion object {
        fun <T> wrapEach(errors: Nel<T>): Nel<ParseError<T>> =
            errors.map { TerminalParseError(it) }
    }
}

data class FocussedError<T>(
    val field: String,
    val error: ParseError<T>,
//    val case: String? = null,
) : ParseError<T>

//data class PotentialCaseError<T>(
//    val subErrors: Nel<ParseError<T>>,
//) : ParseError<T>

infix fun <I, A, E, I2> Parser<I, A, E>.focusByProp(i: KProperty1<I2, I>): Parser<I2, A, ParseError<E>> = Parser { x ->
    parse(i.get(x))
        .mapLeft { errors ->
            errors.map { error ->
                FocussedError(field = i.name, error = TerminalParseError(error))
            }
        }
}

fun <I, A, E> Parser<I, A, ParseError<E>>.focusTerminalError(
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

fun <I, A, E> Parser<I, A, E>.wrapTerminalError(): Parser<I, A, ParseError<E>> = Parser { x ->
    parse(x)
        .mapLeft { errors ->
            errors.map { error ->
                TerminalParseError(error)
            }
        }
}