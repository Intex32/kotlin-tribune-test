package tribune

import arrow.core.*
import com.sksamuel.tribune.core.*
import java.lang.RuntimeException
import kotlin.reflect.KClass

/**
 * Companion for classes that represent parsed results.
 * Facilitates parsing through providing override of [invoke]
 * and promotes a common style.
 * The subclass has to implement the parsing logic.
 */
interface ParserCompanion<I, A, E> {
    val parser: Parser<I, A, E>
    operator fun invoke(x: I) = parser.parse(x)
}

inline fun <I, reified A, E> ParserCompanion<I, A, E>.fromUnsafe(x: I): A =
    parser.parse(x).claimValid()

//abstract class ParserCompanion2<I, A, E>(
//    parser: Parser<I, A, E>,
//) : Parser<I, A, E> by parser {
//    operator fun invoke(x: I) = this.parse(x)
//}

/**
 * Widens [I] to additionally allow null by the type system.
 * Once a null value is received, an error [e] will be passed forward.
 * It widens [I] in the sens that is also accepts nullable values.
 */
fun <I : Any, A, E> Parser<I, A, E>.widenByNullAndFail(e: () -> E): Parser<I?, A, E> = Parser { x ->
    Parser.from<I?>()
        .notNull { e() }
        .parse(x)
        .fold({ errorsNel ->
            Invalid(errorsNel)
        }, { iNotNull ->
            this.parse(iNotNull)
        })
}

/**
 * @see widenByNullAndFail
 */
fun <I : Any, A> Parser<I, A, String>.widenByNullAndFail(): Parser<I?, A, String> =
    widenByNullAndFail { "cannot be null" }

/**
 * Tries all the parsers on [this].
 * Between three cases is distinguished:
 * - exactly one results is [Valid] -> this result is passed forward
 * - multiple results are [Valid] -> if [errAmbiguous] null, the first valid parser's result is used, otherwise a custom [E] from [errAmbiguous] will be used as error
 * - all results are invalid -> if [noValidError] null, all errors are passed forward, otherwise a custom [E] from [noValidError] will be used as error
 */
fun <I, A, E, A2> Parser<I, A, E>.tryParsers(
    errAmbiguous: ((Nel<A2>) -> E)?,
    noValidError: (() -> E)?,
    parser0: Parser<A, A2, E>,
    vararg parsersArr: Parser<A, A2, E>,
): Parser<I, A2, E> = Parser { x ->
    parse(x).fold({ errors ->
        Invalid(errors)
    }, { a ->
        val parsers = NonEmptyList(parser0, parsersArr.toList())
        val parseResults = parsers.map { parser -> parser.parse(a) }

        when (parseResults.count { it.isValid }) {
            1 -> parseResults.first { it.isValid }
            0 -> {
                if (noValidError == null) {
                    val invalids = parseResults.filterIsInstance<Invalid<NonEmptyList<E>>>()
                    Invalid(Nel.fromListUnsafe(invalids.flatMap { it.value }))
                } else
                    noValidError().invalidNel()
            }

            else -> {
                val valids = parseResults.filterIsInstance<Valid<A2>>()
                    .map { it.value }
                    .let { Nel.fromListUnsafe(it) }
                if (errAmbiguous == null)
                    parseResults
                        .first { it.isValid }
                        .getOrElse { error("impossible") }
                        .valid()
                else errAmbiguous(valids).invalidNel()
            }
        }
    })
}

/**
 * @see tryParsers
 */
fun <I, A, E, A2> Parser<I, A, E>.tryParsers(
    parsers: Nel<Parser<A, A2, E>>,
    errAmbiguous: ((Nel<A2>) -> E)?,
    noValidError: (() -> E)?,
): Parser<I, A2, E> =
    tryParsers(errAmbiguous = errAmbiguous, noValidError = noValidError, parsers.head, *parsers.tail.toTypedArray())

/**
 * If parsing [this] was successful, [parser] is subsequently chained and run.
 * If parsing [this] wasn't successful [parser] will never be executed.
 */
inline infix fun <I, A, A2, E> Parser<I, A, E>.andThen(crossinline parser: () -> Parser<A, A2, E>): Parser<I, A2, E> = Parser { i ->
    parse(i).andThen { a -> parser().parse(a) }
}

/**
 * @see andThen
 */
infix fun <I, A, A2, E> Parser<I, A, E>.andThen(parser: Parser<A, A2, E>): Parser<I, A2, E> =
    andThen { parser }

data class FalseClaimParseException @PublishedApi internal constructor(
    val target: KClass<*>,
    val errors: Nel<*>,
    override val message: String = "invalid parse result was claimed to be valid",
) : RuntimeException()

/**
 * Folds [Validated].
 * Throws [FalseClaimParseException] if [Invalid].
 */
inline fun <E, reified A> ValidatedNel<E, A>.claimValid(): A =
    valueOr { errors ->
        throw FalseClaimParseException(
            A::class,
            errors = errors,
        )
    }

/**
 * @see claimValid
 */
@JvmName("claimValid2")
inline fun <E, reified A> ValidatedNel<ParseError<E>, A>.claimValid(): A =
    valueOr { errors ->
        throw FalseClaimParseException(
            target = A::class,
            errors = errors,
            message = "invalid parse result was claimed to be valid; errors: ${A::class.simpleName}: " + errors.joinToString(";\n", "[", "]") { it.toDisplayString() }
        )
    }

/**
 * No matter what the input [I] is,
 * the output will always exactly be [value].
 */
fun <I, A> Parser.Companion.exact(value: A): Parser<I, A, Nothing> =
    from<I>().map { value }

/**
 * Makes null as input a valid value.
 * If input is null, the default [value] is used.
 * Thus, the resulting [Parser] produces a non-null output.
 */
fun <I : Any, A, E> Parser<I, A, E>.widenByNullWithDefault(default: () -> A): Parser<I?, A, E> = this
    .allowNulls()
    .withDefault(default)