package tribune

import com.sksamuel.tribune.core.*
import com.sksamuel.tribune.core.strings.length
import com.sksamuel.tribune.core.strings.notBlank

@JvmInline
value class NonBlankString private constructor(
    val value: String
) {
    companion object : ParserCompanion<String, NonBlankString, String> {
        override val parser = Parser.from<String>()
            .notBlank { "cannot be blank" }
            .map { NonBlankString(it) }
    }
}

@JvmInline
value class DisplayName private constructor(
    val value: String
) {
    companion object : ParserCompanion<String, DisplayName, String> {
        const val MAX_LENGTH = 20

        override val parser = Parser.compose(
            Parser.from<String>().notBlank { "cannot be blank" },
            Parser.from<String>().length({ it <= MAX_LENGTH }) { "max length is $MAX_LENGTH" },
        ) { s, _ -> s }.map(::DisplayName)
    }
}

@JvmInline
value class Email private constructor(val value: String) {
    companion object : ParserCompanion<String, Email, String> {
        val REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}\$")

        override val parser = Parser.from<String>()
            .filter({ REGEX.matches(it) }) { "does not match regex pattern" }
            .map { Email(it) }
    }
}

@JvmInline
value class NonNegInt private constructor(val value: Int) {
    companion object : ParserCompanion<Int, NonNegInt, String> {
        override val parser = Parser.from<Int>()
            .nonNegative { "cannot be negative" }
            .map { NonNegInt(it) }

        operator fun invoke(x: UInt) = NonNegInt(x.toInt())
    }
}