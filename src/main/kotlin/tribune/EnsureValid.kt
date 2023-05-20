package tribune

import arrow.core.*
import arrow.core.continuations.EffectScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * extracting the common code of all functions in this file
 */
@PublishedApi
internal suspend fun <ERROR, ACC_ERROR> EffectScope<ACC_ERROR>.ensureAllValidInternal(
    all: Nel<ValidatedNel<ERROR, Any?>>,
    mapAccErrors: (Nel<ERROR>) -> ACC_ERROR,
) {
    val invalids = all.filterIsInstance<Invalid<Nel<ERROR>>>().toNonEmptyListOrNone()
        .getOrElse { return }
    val accumulatedErrors = invalids
        .flatMap { it.value }
        .let(mapAccErrors)
    shift<Nothing>(accumulatedErrors)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, ACC_ERROR, reified A> EffectScope<ACC_ERROR>.ensureAllValid(a: ValidatedNel<ERROR, A>, noinline mapAccErrors: (Nel<ERROR>) -> ACC_ERROR) {
    contract {
        returns() implies (a is Valid<A>)
    }
    ensureAllValidInternal(nonEmptyListOf(a), mapAccErrors)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>) {
    contract {
        returns() implies (a is Valid<A>)
    }
    ensureAllValidInternal(nonEmptyListOf(a), ::identity)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, ACC_ERROR, reified A, reified B> EffectScope<ACC_ERROR>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, noinline mapAccErrors: (Nel<ERROR>) -> ACC_ERROR) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b), mapAccErrors)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b), ::identity)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, ACC_ERROR, reified A, reified B, reified C> EffectScope<ACC_ERROR>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, c: ValidatedNel<ERROR, C>, noinline mapAccErrors: (Nel<ERROR>) -> ACC_ERROR) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c), mapAccErrors)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B, reified C> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, c: ValidatedNel<ERROR, C>) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c), ::identity)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, ACC_ERROR, reified A, reified B, reified C, reified D> EffectScope<ACC_ERROR>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, c: ValidatedNel<ERROR, C>, d: ValidatedNel<ERROR, D>, noinline mapAccErrors: (Nel<ERROR>) -> ACC_ERROR) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C> && d is Valid<D>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c, d), mapAccErrors)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B, reified C, reified D> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, c: ValidatedNel<ERROR, C>, d: ValidatedNel<ERROR, D>) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C> && d is Valid<D>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c, d), ::identity)
}