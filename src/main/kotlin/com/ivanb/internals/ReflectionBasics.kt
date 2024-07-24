package com.ivanb.internals

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmName

/**
 * # Reflection
 * The ability to inspect and invoke functionality dynamically at runtime
 *
 * */
object ReflectionBasics {
    data class Person(
        val name: String,
        val age: Int,
    ) {
        var favouriteMovie: String = "Forrest gump"

        fun fillInTaxForm(authority: String) = "[$name] Filing taxes for $authority..."
        constructor(name: String) : this(name, 0)

        companion object {
            val CAN_FLY = false

            fun fromCSV(csv: String): Person? {
                val tokens = csv.split(",").map(String::trim)
                if (tokens.size != 2) {
                    return null
                }
                return Person(tokens[0], tokens[1].toInt())
            }
        }
    }

    val personClass: KClass<Person> = Person::class

    @JvmStatic
    fun main(args: Array<String>) {
        // class reference => class name, methods, properties
        println("------------- Class basic info ----------------")
        println("Class name: ${personClass.simpleName}")
        println("JVM name: ${personClass.jvmName}")
        println("JVM name: ${personClass.qualifiedName}")

        println("Class is final: ${personClass.isFinal}")
        println("Access modifier: ${personClass.visibility}")

        val properties = personClass.declaredMemberProperties
        println("--------------------Class properties-------------------- ")
        properties.forEach {
            println("Name: ${it.name}, type: ${it.returnType.classifier}, is nullable: ${it.returnType.isMarkedNullable}")
        }
        val person = Person("Daniel", 55)

        println(" --------------------- Instance properties ---------------------")
        val instanceProperties =
            properties.map {
                "${it.name} -> ${it.call(person)}"
            }
        instanceProperties.forEach { println(it) }

        val favouriteMovieProp = properties.find { it.name.lowercase().contains("movie") }
        if (favouriteMovieProp != null && favouriteMovieProp is KMutableProperty<*>) {
            favouriteMovieProp.setter.call(person, "Dune")
        }

        println(person.favouriteMovie)

        val functions = personClass.declaredFunctions
        println("--------------------Class functions-------------------- ")
        functions.forEach { fn ->
            println("Function ${fn.name}: ${fn.parameters.joinToString(", ") {it.type.toString() }} -> ${fn.returnType}")
        }

        val taxFunc = functions.find { it.name.lowercase().contains("tax") }
        if (taxFunc != null) {
            // first arg is always teh instance on which the function is called
            println(taxFunc.call(person, "Tax authority"))
        }

        // companion objects

        println("--------Companion objects-----------------")
        val companionType = personClass.companionObject // can inspect properties and methods as well
        val companionObject = personClass.companionObjectInstance // is possible because the companion object is a singleton
        companionType?.declaredMemberProperties?.forEach {
            println("Name: ${it.name}, type: ${it.returnType}, value: ${it.call(companionObject)}")
        } ?: println("Nothing (companion is null)")
    }
}
