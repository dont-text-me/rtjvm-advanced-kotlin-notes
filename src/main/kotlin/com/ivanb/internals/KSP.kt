package com.ivanb.internals

import com.ivanb.builderksp.Builder

/**
 * # KSP
 *
 *  - analyze source code and generate new source code
 *  - compile
 *  - access methods/functionality at **compile time**
 * */

@Builder
data class Person(
    val name: String,
    val age: Int,
)

@Builder
data class Pet(
    val name: String,
    val nickname: String,
)

object KSP {
    // use-case: generate builder patterns for data classes
    // module 1 - source + place where the generated source will be created
    // module 2 - symbol definitions (annotations)
    // module 3 - KSP logic for generating the source

    @JvmStatic
    fun main(args: Array<String>) {
        val myPerson = PersonBuilder().name("Ivan").age(23).build()
        val myPet = PetBuilder().name("Canine the destroyer").nickname("Doggy").build()
        println(myPerson)
        println(myPet)
    }
}
