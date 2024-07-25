package com.ivanb.practice

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object DIFramework {
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Layer

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Inject

    // Layers: controller - http requests, service - business logic, repository - data layer

    @Layer
    class Repository {
        fun getData(): String = "Data from repository"
    }

    @Layer
    class Service {
        @Inject lateinit var repository: Repository

        fun performAction(): String = repository.getData() + " with some business logic"
    }

    @Layer
    class UserManager {
        private val loggedUsers = mutableSetOf<String>()

        fun login(username: String) {
            loggedUsers.add(username)
            println("[log] Logged in as $username")
        }

        fun isLoggedIn(username: String) = username in loggedUsers

        fun logout(username: String) {
            loggedUsers.remove(username)
            println("[log] Logged out user $username")
        }
    }

    @Layer
    class Controller {
        @Inject lateinit var service: Service

        @Inject lateinit var users: UserManager

        fun processHTTPRequest(
            payload: String,
            username: String = "invalid@myemail.com",
        ): String =
            if (users.isLoggedIn(username)) {
                "Processed request! Response: ${service.performAction()}"
            } else {
                "Not logged in, request denied"
            }
    }

    class DIManager {
        private val layers = mutableMapOf<KClass<*>, Any>()

        private fun <T : Any> register(clazz: KClass<T>) {
            if (clazz.findAnnotation<Layer>() != null) {
                val instance = clazz.createInstance()
                layers[clazz] = instance
            }
        }

        private fun <T : Any> injectDependencies(instance: T) =
            instance::class
                .declaredMemberProperties
                .filter { it.findAnnotation<Inject>() != null }
                .filterIsInstance<KMutableProperty<*>>()
                .forEach {
                    it.setter.call(instance, layers[it.returnType.classifier as KClass<*>])
                }

        fun initialize() {
            DIFramework::class.nestedClasses.forEach { register(it) }
            this.layers.values.forEach { injectDependencies(it) }
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> get(clazz: KClass<T>): T? = layers[clazz] as? T
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val diManager = DIManager()

        diManager.initialize()

        val controller = diManager.get(Controller::class)

        val userService = diManager.get(UserManager::class)

        userService?.login("me@myemail.com")

        println(
            controller?.processHTTPRequest(
                """{"source":"sensors/incremental"}""",
                "me@myemail.com",
            ),
        )















        userService?.logout("me@myemail.com")

        println(
            controller?.processHTTPRequest(
                """{"source":"sensors/incremental"}""",
                "me@myemail.com",
            ),
        )
    }
}
