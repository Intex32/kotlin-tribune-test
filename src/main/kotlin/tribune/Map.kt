package tribune

import com.sksamuel.tribune.core.Parser

fun <K, V> Parser.Companion.fromMap(): Parser<Map<K, V>, Map<K, V>, Nothing> =
    from()