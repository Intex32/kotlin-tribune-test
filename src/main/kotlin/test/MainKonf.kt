package test

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.compose
import com.sksamuel.tribune.core.filter
import tribune.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class KonfPostDto(
    val name: String,
    val text: String,
    val magicNumber: Int,
    val start: LocalDate,
    val end: LocalDate,
)

data class KonfCreateCmd(
    val name: DisplayName,
    val text: NonBlankString,
    val magicNumber: Int,
    val zeitraum: OrderedClosedRange<LocalDate>,
) {
    companion object : ParserCompanion<KonfCreateCmd, ValidatedByParser<KonfCreateCmd>, String> {
        override val parser = @OptIn(DeliberatelyValidated::class) Parser.from<KonfCreateCmd>()
            .filter({ x ->
                ChronoUnit.DAYS.between(x.zeitraum.value.start, x.zeitraum.value.endInclusive) < x.magicNumber
            }) { "Spanne des Zeitraums muss kleiner sein als die magische Zahl" }
            .declareValidated()
    }
}

fun main(args: Array<String>) {
    val input = KonfPostDto(
        name = "                                                                                                ",
        text = "  ",
        magicNumber = 42,
        start = LocalDate.now(),
        end = LocalDate.now().plusDays(-1),
    )
    endpoint(input)

    val input2 = KonfPostDto(
        name = "burglary",
        text = "meso",
        magicNumber = 42,
        start = LocalDate.now(),
        end = LocalDate.now().plusDays(7),
    )
    endpoint(input2)
}

fun endpoint(input: KonfPostDto) {
    val seife = @OptIn(DeliberatelyValidated::class) Parser.from<KonfPostDto>()
        .filter({ x -> x.magicNumber != 42}) { TerminalParseError("cannot be 42") }
        .declareValidated()

    val sdf: Parser<KonfPostDto, Int, ParseError<String>> = Parser.from<Int>().widenByProp<Int, Int, String, KonfPostDto>(KonfPostDto::magicNumber)

    val konfParser: EParser<KonfPostDto, ValidatedByParser<KonfCreateCmd>, String> = Parser.compose(
        DisplayName.parser widenByProp KonfPostDto::name,
        NonBlankString.parser widenByProp KonfPostDto::text,
        Parser.fromAndWidenByProp(KonfPostDto::magicNumber), // short for Parser.from<Int>() focusByProp KonfPostDto::magicNumber,
        OrderedClosedRange.parser<LocalDate>()
            .contramap<KonfPostDto> { it.start..it.end }
            .wrapTerminalError()
            .focusErrorsByProps(KonfPostDto::start, KonfPostDto::end),
        ::KonfCreateCmd,
    ).andThen(
        KonfCreateCmd.parser
            .wrapTerminalError()
    )

    printParseResult(konfParser.parse(input))
}