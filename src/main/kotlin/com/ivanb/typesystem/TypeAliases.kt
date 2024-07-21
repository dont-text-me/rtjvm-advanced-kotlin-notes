package com.ivanb.typesystem
// Type aliases must be declared top-level

typealias PhoneBook = Map<String, String>
typealias Table<A> = Map<String, A>

class Either<out E, out A>

// variance modifiers carry over to type aliases
typealias ErrorOr<A> = Either<Throwable, A>

object TypeAliases {
    val phoneBook: PhoneBook = mapOf("someone" to "123-456")

    val myMap: Map<String, String> = phoneBook

    val stringTable: Table<String> = phoneBook

    @JvmStatic
    fun main(args: Array<String>) {
    }
}
