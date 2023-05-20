package test

import arrow.core.*
import arrow.core.continuations.either
import com.sksamuel.tribune.core.*
import com.sksamuel.tribune.core.strings.length
import com.sksamuel.tribune.core.strings.notBlank
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tribune.*

private data class PasswordInput(
    val password: String,
    val passwordConfirmed: String,
)

private data class Password(
    val password: NonBlankString,
    val passwordConfirmed: NonBlankString,
)

private data class BookInput(
    val title: String,
    val genre: String?,
)

private data class Book(
    val title: DisplayName,
    val genre: NonBlankString,
    val summary: NonBlankString,
)

@JvmInline
private value class ConfinedTypeWithManyConstraints private constructor(
    val value: String,
) {
    companion object : ParserCompanion<String, ConfinedTypeWithManyConstraints, String> {
        override val parser = Parser
            .fromAndMap<String, _> { it.trim() }
            .andThen {
                Parser.compose(
                    Parser.from<String>().notBlank { "not blank" },
                    Parser.from<String>().filter({ it.startsWith("a")}) { "starts with a" },
                    Parser.from<String>().filter({ it.endsWith("z")}) { "ends with z" },
                    Parser.from<String>().length({ it <= 3 }) { "max length 3" },
                ) { s, _, _, _ -> ConfinedTypeWithManyConstraints(s) }
            }
            /*.compose<String, String, String>(
                Parser.from(),
                { it.notBlank { "not blank" }.map {} },
                { it.filter({ it.startsWith("a") }) { "starts with a" }.map { } },
                { it.filter({ it.endsWith("z") }) { "ends with z" }.map { } },
                { it.length({ it <= 3 }) { "max length 3" }.map { } },
            )
            .map(::ConfinedTypeWithManyConstraints)*/
    }
}

class MiscTests : AnnotationSpec() {

    @Test
    fun unsafe() {
        assertDoesNotThrow {
            DisplayName.fromUnsafe("valid name")
        }
        assertThrows<FalseClaimParseException> {
            NonBlankString.fromUnsafe(" ")
        }
    }

    @Test
    fun `display name trims input`() {
        DisplayName
            .fromUnsafe(" test ")
            .value
            .shouldBeEqual("test")
    }

    @Test
    fun `confined type returns all accumulated errors`() {
        ConfinedTypeWithManyConstraints("meso")
            .shouldBeInstanceOf<Invalid<Nel<*>>>()
            .value
            .shouldHaveSize(3)
    }

    @Test
    fun deliberatelyValidated() {
        val result: ValidatedNel<ParseError<String>, ValidatedByParser<Password>> = Parser.compose(
            NonBlankString.parser widenByProp PasswordInput::password,
            NonBlankString.parser widenByProp PasswordInput::passwordConfirmed,
            ::Password,
        ).andThen {
            @OptIn(DeliberatelyValidated::class)
            Parser.from<Password>()
                .filter({ it.password == it.passwordConfirmed }) { "passwords dont match" }
                .wrapTerminalError()
                .declareValidated()
        }.parse(
            PasswordInput(
                password = "pwd",
                passwordConfirmed = "pwd2",
            )
        )
        printParseResult(result)
    }

    @Test
    fun `hybrid parse (not only from input)`() {
        val defaultGenre = NonBlankString.fromUnsafe("horror")
        val hardcodedSummary = NonBlankString.fromUnsafe("summary placeholder")

        val parser = Parser.compose(
            DisplayName.parser widenByProp BookInput::title,
            NonBlankString.parser
                .widenByNullWithDefault { defaultGenre }
                .widenByProp(BookInput::genre),
            Parser.exact(hardcodedSummary),
            ::Book,
        )

        BookInput(
            title = "Uncle Bob's Cottage",
            genre = "thriller",
        ).let(parser::parse).apply {
            shouldBeTypeOf<Valid<Book>>()
            value.summary shouldBeEqual hardcodedSummary
        }

        BookInput(
            title = "Uncle Bob's Cottage",
            genre = null,
        ).let(parser::parse).apply {
            shouldBeTypeOf<Valid<Book>>()
            value.genre shouldBeEqual defaultGenre
        }
    }

    @Test
    suspend fun `flat zip and accumulate`() {
        either {
            val a: ValidatedNel<String, String> = "meso".validNel()
            val b: ValidatedNel<String, String> = "merism".validNel()

            ensureAllValid(a, b)

            a.value
            b.value
            //c.value // does not compile

            a.value + b.value
        } shouldBeEqual "mesomerism".right()
    }

    @Test
    suspend fun `double flat zip and accumulate`() {
        either {
            val a: ValidatedNel<String, String> = "fro".validNel()
            val b: ValidatedNel<String, String> = "ma".validNel()
            val c: ValidatedNel<String, String> = "ge".validNel()

            ensureAllValid(a, b)
            a.value
            b.value

            ensureAllValid(a, c)
            c.value

            a.value + b.value + c.value
        } shouldBeEqual "fromage".right()
    }

    @Test
    suspend fun `flat zip and accumulate fail`() {
        either {
            val a: ValidatedNel<String, String> = "fromage".validNel()
            val b: ValidatedNel<String, String> = Invalid("error".nel())

            ensureAllValid(a, b)

            a.value + b.value
        } shouldBeEqual "error".nel().left()
    }

}