package test

import com.sksamuel.tribune.core.Parser
import com.sksamuel.tribune.core.compose
import tribune.*

data class PersonInput(
    val name: String,
    val email: String,
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
    val email: Email,
    val address: Address,
)

data class Address(
    val country: NonBlankString,
    val street: Street,
)

data class Street(
    val name: NonBlankString,
    val houseNumber: NonNegInt,
)

fun main() {
    val streetParser: EParser<StreetInput, Street, String> = Parser.compose(
            NonBlankString.parser widenByProp StreetInput::name,
            NonNegInt.parser widenByProp StreetInput::houseNumber,
            ::Street,
    )
    val addressParser: EParser<AddressInput, Address, String> = Parser.compose(
            NonBlankString.parser widenByProp AddressInput::country,
            streetParser widenByProp AddressInput::street,
            ::Address,
    )
    val personParser: EParser<PersonInput, Person, String> = Parser.compose(
            DisplayName.parser widenByProp PersonInput::name,
            Email.parser widenByProp PersonInput::email,
            addressParser widenByProp PersonInput::address,
            ::Person,
    )

    val personInput = PersonInput(
        name = "John Michael Patrick George Smith",
        email = "barryba@gmail.com",
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