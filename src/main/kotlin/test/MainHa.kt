package test

import arrow.core.Validated
import arrow.core.nonEmptyListOf
import com.sksamuel.tribune.core.*
import tribune.*
import java.time.LocalDate

data class HaPostDto(
    val text: String,
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate? = null,
    val endTimes: Int? = null,
)

data class HaCreateCmd(
    val text: NonBlankString,
    val end: End,
) {
    sealed interface End {
        data class Date(
            val zeitraum: OrderedClosedRange<LocalDate>,
        ) : End

        data class CountIntoFuture(
            val start: LocalDate,
            val times: Amount,
        ) : End
    }
}

fun main(args: Array<String>) {
    HaPostDto(
        text = "  ",
        start = LocalDate.now(),
        end = LocalDate.now().plusDays(-2),
    ).also(::endpoint)

    HaPostDto(
        text = "Test Anzahl",
        start = LocalDate.now(),
        endTimes = -2,
    ).also(::endpoint)

    HaPostDto(
        text = "TEST",
        start = LocalDate.now(),
        end = LocalDate.now().plusDays(2),
        endTimes = 5,
    ).also(::endpoint)
}

fun endpoint(input: HaPostDto) {
    val haEndDateParser = Parser.from<HaPostDto>()
        .filter({ it.end != null }) { "End is null" }
        .wrapTerminalError()
        .andThen(
            OrderedClosedRange.parser<LocalDate>()
                .contramap<HaPostDto> { it.start..it.end!! }
                .wrapTerminalError()
                .focusTerminalError(HaPostDto::start, HaPostDto::end)
                .map { HaCreateCmd.End.Date(it) }
        )

    val haEndAnzahlParser: EParser<HaPostDto, HaCreateCmd.End.CountIntoFuture, String> = Parser.compose(
        Parser.from<LocalDate>().contramap<HaPostDto> { it.start }.wrapTerminalError(),
        Amount.parser
            .widenByFailOnNull()
            .focusByProp(HaPostDto::endTimes),
        HaCreateCmd.End::CountIntoFuture,
    )
    val haParser: EParser<HaPostDto, HaCreateCmd, String> =
        Parser.compose(
            NonBlankString.parser focusByProp HaPostDto::text,
            Parser.from<HaPostDto>().tryParsers(
                parsers = nonEmptyListOf(haEndDateParser, haEndAnzahlParser),
                errAmbiguous = { TerminalParseError("mehrdeutig welcher Modus für Enddatum") },
                noValidError = { TerminalParseError("kein Modus für Enddatum identifizierbar") },
            ),
            ::HaCreateCmd,
        )

    when (val parsed = haParser.parse(input)) {
        is Validated.Valid -> println(parsed.value)
        is Validated.Invalid -> println(parsed.value.joinToString("; ") { it.toDisplayString() })
    }
}