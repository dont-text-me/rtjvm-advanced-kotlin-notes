package com.ivanb.internals

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object ReflectionAnnotations {
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    // SOURCE is only inspeccted by source tools e.g compiler
    // BINARY - copied to the binary
    // RUNTIME - copied to the binary AND can be inspected via reflection
    annotation class TestAnnotation(
        val value: String,
    )

    @TestAnnotation(value = "Example") // TestAnnotation instance per class *declaration*
    class AnnotatedClass

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Table(
        val name: String,
    )

    @Target(AnnotationTarget.PROPERTY)
    annotation class Column(
        val name: String,
    )

    @Table("users")
    data class User(
        @Column("user_id") val id: Int,
        @Column("user_name") val name: String,
        @Column("user_age") val age: Int,
    )

    fun generateTableStatement(clazz: KClass<*>): String? {
        val tableAnnotation: Table? = clazz.findAnnotation<Table>()

        val tableName = tableAnnotation?.name ?: return null
        val columns =
            clazz.declaredMemberProperties.mapNotNull {
                val columnAnnotation = it.findAnnotation<Column>()
                val columnName = columnAnnotation?.name
                val columnType =
                    when (it.returnType.classifier) {
                        Int::class -> "INTEGER"
                        String::class -> "TEXT"
                        else -> null
                    }
                if (columnName == null || columnType == null) null else "$columnName $columnType"
            }
        return "CREATE TABLE $tableName ${columns.joinToString(", ", "(", ")")};"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val createTableStatement = generateTableStatement(User::class)
        println(createTableStatement)
    }
}
