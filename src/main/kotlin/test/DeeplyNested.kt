package test

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.compose
import tribune.*

data class PersonInput(
        val name: String,
        val address: AddressInput,
)

data class AddressInput(
        val country: String,
        val street: StreetInput,
)

data class StreetInput(
        val name: String,
        val houseNumber: Int,
)


data class Person(
        val name: DisplayName,
        val address: Address,
)

data class Address(
        val country: NonBlankString,
        val street: Street,
)

data class Street(
        val name: NonBlankString,
        val houseNumber: Amount,
)

fun main() {
    val streetParser: EParser<StreetInput, Street, String> = Parser.compose(
            NonBlankString.parser focusByProp StreetInput::name,
            Amount.parser focusByProp StreetInput::houseNumber,
            ::Street,
    )
    val addressParser: EParser<AddressInput, Address, String> = Parser.compose(
            NonBlankString.parser focusByProp AddressInput::country,
            streetParser focusByProp AddressInput::street,
            ::Address,
    )
    val personParser: EParser<PersonInput, Person, String> = Parser.compose(
            DisplayName.parser focusByProp PersonInput::name,
            addressParser focusByProp PersonInput::address,
            ::Person,
    )

    val personInput = PersonInput(
            name = "hans joachiam walter luca ben daniel schneesturm sengelmann",
            address = AddressInput(
                    country = "",
                    street = StreetInput(
                            name = "Rosenstrase",
                            houseNumber = -2,
                    ),
            ),
    )
    val parsedPerson = personParser.parse(personInput)
    printParseResult(parsedPerson)
}