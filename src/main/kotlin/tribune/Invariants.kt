package tribune

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.filter
import com.sksamuel.tribune.core.map

/**
 * [ClosedRange] with constraint that
 * end has to be greater or equal start.
 */
@JvmInline
value class OrderedClosedRange<T : Comparable<T>> private constructor(
    val value: ClosedRange<T>,
) {
    companion object {
        fun <T : Comparable<T>> parser() = Parser.from<ClosedRange<T>>()
            .filter({ it.endInclusive >= it.start }) { "end has to be >= start" }
            .map { OrderedClosedRange(it) }
    }
}
