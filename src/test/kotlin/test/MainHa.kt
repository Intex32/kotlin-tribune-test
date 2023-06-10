package test

import arrow.core.nonEmptyListOf
import com.sksamuel.tribune.core.*
import tribune.*
import java.time.LocalDate

data class HaPostDto(
    val authorId: Long,
    val text: String,
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate? = null,
    val endTimes: Int? = null,
)

data class HaCreateCmd(
    val authorId: Long,
    val text: NonBlankString,
    val end: End,
) {
    sealed interface End {
        data class Date(
            val zeitraum: OrderedClosedRange<LocalDate>,
        ) : End

        data class CountIntoFuture(
            val start: LocalDate,
            val times: NonNegInt,
        ) : End
    }
}

fun main(args: Array<String>) {
    HaPostDto(
        authorId = 32,
        text = "  ",
        start = LocalDate.now(),
        end = LocalDate.now().plusDays(-2),
    ).also(::endpoint)

    HaPostDto(
        authorId = 32,
        text = "Test Anzahl",
        start = LocalDate.now(),
        endTimes = 6,
    ).also(::endpoint)

    HaPostDto(
        authorId = 32,
        text = "TEST",
        start = LocalDate.now(),
        end = LocalDate.now().plusDays(2),
        endTimes = 5,
    ).also(::endpoint)

    internalService()
}

private fun internalService() {
    HaCreateCmd(
        authorId = 23,
        text = NonBlankString.fromUnsafe("  "),
        end = HaCreateCmd.End.Date(
            zeitraum = OrderedClosedRange.parser<LocalDate>()
                .parse(LocalDate.now()..LocalDate.now().plusDays(-2))
                .claimValid()
        ),
    )
}

private fun endpoint(input: HaPostDto) {
    val haEndDateParser = Parser.from<HaPostDto>()
        .filter({ it.end != null }) { "end is null" }
        .wrapTerminalError()
        .andThen(
            OrderedClosedRange.parser<LocalDate>()
                .contramap<HaPostDto> { it.start..it.end!! }
                .wrapTerminalError()
                .focusErrorsByProps(HaPostDto::start, HaPostDto::end)
                .map { HaCreateCmd.End.Date(it) }
        )

    val haEndAnzahlParser: EParser<HaPostDto, HaCreateCmd.End.CountIntoFuture, String> = Parser.compose(
        Parser.from<LocalDate>().contramap<HaPostDto> { it.start }.wrapTerminalError(),
        NonNegInt.parser
            .widenByNullAndFail()
            .widenByProp(HaPostDto::endTimes),
        HaCreateCmd.End::CountIntoFuture,
    )
    val haParser: EParser<HaPostDto, HaCreateCmd, String> =
        Parser.compose(
            Parser.fromAndWidenByProp(HaPostDto::authorId),
            NonBlankString.parser widenByProp HaPostDto::text,
            Parser.from<HaPostDto>().tryParsers(
                parsers = nonEmptyListOf(haEndDateParser, haEndAnzahlParser),
                errAmbiguous = { TerminalParseError("mode for end date is ambiguous") },
                noValidError = { TerminalParseError("no mode for end date recognized") },
            ),
            ::HaCreateCmd,
        )

    printParseResult(haParser.parse(input))
}