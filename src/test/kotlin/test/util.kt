package test

import arrow.core.Invalid
import arrow.core.Valid
import arrow.core.ValidatedNel
import tribune.ParseError

inline fun <E, reified A> printParseResult(x: ValidatedNel<ParseError<E>, A>) = when(x) {
    is Valid -> "parsed value of ${A::class.simpleName}: ${x.value}"
    is Invalid -> "errors while parsing ${A::class.simpleName}: " + x.value.joinToString(";\n", "[\n", "\n]") {"    " + it.toDisplayString() }
}.also(::println)