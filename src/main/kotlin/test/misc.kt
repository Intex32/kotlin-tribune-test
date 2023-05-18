package test

import tribune.DisplayName
import tribune.NonBlankString
import tribune.fromUnsafe

fun main() {
    unsafe()
}

private fun unsafe() {
    DisplayName.fromUnsafe("  test ")
        .also { println(it) }
    NonBlankString.fromUnsafe("  ")
}