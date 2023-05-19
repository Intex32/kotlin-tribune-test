package test

import arrow.core.Valid
import arrow.core.ValidatedNel
import com.sksamuel.tribune.core.*
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.equals.shouldBeEqual
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
            value.summary.value shouldBeEqual hardcodedSummary
        }

        BookInput(
            title = "Uncle Bob's Cottage",
            genre = null,
        ).let(parser::parse).apply {
            shouldBeTypeOf<Valid<Book>>()
            value.genre.value shouldBeEqual defaultGenre
        }
    }

}