package test

import tribune.NonBlankString

data class PersonInput(
    val address: AddressInput,
)
data class AddressInput(
    val street: StreetInput,
    val country: String,
)
data class StreetInput(
    val name: String,
    val houseNumber: Int,
)


data class Person(
    val address: Address,
)
data class Address(
    val street: Street,
    val country: NonBlankString,
)
data class Street(
    val name: NonBlankString,
    val houseNumber: Int,
)

fun main() {
    //TODO
}