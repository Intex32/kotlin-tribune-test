package tribune

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.map

/**
 * No matter what the input [I] is,
 * the output will always exactly be [value].
 */
fun <I, A> Parser.Companion.exact(value: A): Parser<I, A, Nothing> =
    from<I>().map { value }

/**
 * convenience function
 */
fun <I, A> Parser.Companion.fromAndMap(f: (I) -> A): Parser<I, A, Nothing> =
    from<I>().map(f)