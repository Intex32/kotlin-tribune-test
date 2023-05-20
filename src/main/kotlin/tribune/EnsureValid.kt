package tribune

import arrow.core.*
import arrow.core.continuations.EffectScope
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * extracting the common code of all functions in this file
 */
@PublishedApi
internal suspend fun <ERROR> EffectScope<Nel<ERROR>>.ensureAllValidInternal(all: Nel<ValidatedNel<ERROR, Any?>>) {
    val invalids = all.filterIsInstance<Invalid<Nel<ERROR>>>().toNonEmptyListOrNone()
        .getOrElse { return }
    val accumulatedErrors = invalids.flatMap { it.value }
    shift<Nothing>(accumulatedErrors)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>) {
    contract {
        returns() implies (a is Valid<A>)
    }
    ensureAllValidInternal(nonEmptyListOf(a))
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b))
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B, reified C> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, c: ValidatedNel<ERROR, C>) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c))
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B, reified C, reified D> EffectScope<Nel<ERROR>>.ensureAllValid(a: ValidatedNel<ERROR, A>, b: ValidatedNel<ERROR, B>, c: ValidatedNel<ERROR, C>, d: ValidatedNel<ERROR, D>) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C> && d is Valid<D>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c, d))
}

@OptIn(ExperimentalContracts::class)
suspend inline fun <ERROR, reified A, reified B, reified C, reified D, reified E> EffectScope<Nel<ERROR>>.ensureAllValid(
    a: ValidatedNel<ERROR, A>,
    b: ValidatedNel<ERROR, B>,
    c: ValidatedNel<ERROR, C>,
    d: ValidatedNel<ERROR, D>,
    e: ValidatedNel<ERROR, E>
) {
    contract {
        returns() implies (a is Valid<A> && b is Valid<B> && c is Valid<C> && d is Valid<D> && e is Valid<E>)
    }
    ensureAllValidInternal(nonEmptyListOf(a, b, c, d, e))
}
