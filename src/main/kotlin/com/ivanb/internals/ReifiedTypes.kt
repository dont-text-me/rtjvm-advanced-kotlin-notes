package com.ivanb.internals

object ReifiedTypes {
    // will not work by itself - JVM type erasure
    // no generics in java pre-5 (2004)
    // fun <T> filterByType(list: List<Any>): List<T> = list.filter { it is T }.map{it as T}
    // solution is  inline functions + reified types

    inline fun <reified T> List<Any>.filterByType(): List<T> = this.filter { it is T }.map { it as T }

    data class Person(
        val name: String,
        val age: Int,
    )

    data class Car(
        val make: String,
        val model: String,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val mixedList: List<Any> =
            listOf(
                Person("John", 30),
                Car("Toyota", "Corolla"),
                Person("Jane", 29),
                "my string",
                10,
                "another string",
            )

        val people: List<Person> = mixedList.filterByType()
        // compiles - because the function is inlined so type is available at compile time
    }
}
