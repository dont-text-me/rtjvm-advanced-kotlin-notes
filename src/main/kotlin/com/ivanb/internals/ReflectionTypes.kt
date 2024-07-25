package com.ivanb.internals

import java.io.File
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

object ReflectionTypes {
    // example: avoid/circumvent type erasure
//    fun processList(list: List<*>) = when(list){
//        is List<String> -> list.forEach { println(it) }
//        is List<Int> -> println(list.sum())
//        else -> println("Not supported")
//    }

    fun processList(
        list: List<*>,
        type: KType,
    ) {
        if (type.isSubtypeOf(typeOf<List<String>>())) {
            println("Processing a list of strings")
        } else if (type.isSubtypeOf(typeOf<List<Int>>())) {
            println("Processing a list of ints")
        } else {
            println("Not supported")
        }
    }

    inline fun <reified T> processListGeneric(list: List<T>) = processList(list, typeOf<List<T>>())

    fun processListV2(list: List<*>) {
        val typeParams = list::class.typeParameters.map { it.name }
        println(typeParams)
        if (typeParams.contains("String")) {
            println("Processing list of strings")
        } else if (typeParams.contains("Int")) {
            println("Processing list of ints")
        } else {
            println("Not supported")
        }
    }

    data class MyConfig(
        val host: String,
        val port: Int,
        val debug: Boolean,
        val maxConnections: Int,
        val timeout: Double,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val config = ConfigLoader.default().loadAs<MyConfig>()
        println(config)
    }
}

class ConfigLoader private constructor(
    val path: String = "src/main/resources/application.conf",
) {
    fun parseFile(): Map<String, String> {
        val file = File(path)
        val configMap = mutableMapOf<String, String>()
        file.forEachLine {
            val trimmed = it.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val (key, value) = trimmed.split("=").map(String::trim)
                configMap[key] = value
            }
        }
        return configMap
    }

    fun deserializeValue(
        value: String,
        type: KType,
    ): Any =
        when (type.classifier) { // Ktype.classifier -> KClassifier (supertype of KClass)
            String::class -> value
            Int::class -> value.toInt()
            Boolean::class -> value.toBoolean()
            Double::class -> value.toDouble()
            else -> throw IllegalArgumentException("Unsupported type: $type")
        }

    inline fun <reified T : Any> deserializeObject(props: Map<String, String>): T {
        val kClass = T::class
        val constructor =
            kClass.primaryConstructor
                ?: throw IllegalArgumentException("Type ${kClass.simpleName} does not have an accessible primary constructor")
        val args: Map<KParameter, Any> =
            constructor.parameters.associateWith { param ->
                val key = param.name ?: throw IllegalArgumentException("Unnamed constructor param for ${kClass.simpleName}")
                val value =
                    props[key]
                        ?: throw IllegalArgumentException("Missing value for constructor param ${param.name} in class ${kClass.simpleName}")
                deserializeValue(value, param.type)
            }
        return constructor.callBy(args)
    }

    inline fun <reified T : Any> loadAs(): T {
        val props = parseFile()
        return deserializeObject<T>(props)
    }

    companion object {
        fun default() = ConfigLoader()

        fun at(path: String) = ConfigLoader(path)
    }
}
