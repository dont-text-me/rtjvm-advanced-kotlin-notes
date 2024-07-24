package com.ivanb.internals

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.random.Random

/**
 * # Contracts
 * A contract is code that helps the compiler make some deductions i.e. info about the return value of a function
 * */
object Contracts {
    @OptIn(ExperimentalContracts::class)
    fun containsJustDigits(str: String?): Boolean {
        contract {
            // small DSL for testing things about this function
            returns(true) implies (str != null)
        }

        return str?.all { it.isDigit() } ?: false
    }

    fun demoNullableString() {
        val maybeString: String? = if (Random.nextBoolean()) "123456" else null
        println("Maybe string: $maybeString")
        if (containsJustDigits(maybeString)) {
            println("String is just numbers, i want the length: ${maybeString.length}")
        }
    }

    open class User(
        open val username: String,
        open val email: String,
    ) {
        @OptIn(ExperimentalContracts::class)
        fun isValidAdmin(): Boolean {
            contract {
                returns(true) implies (this@User is Admin)
            }
            return this is Admin && email.endsWith("@myemail.com")
        }
    }

    class Admin(
        override val username: String,
        override val email: String,
        val permissions: List<String>,
    ) : User(username, email) {
        fun purgeData() = println("All data removed")
    }

    fun attemptAdminTasks(user: User) {
        if (user.isValidAdmin()) {
            println("Running admin tasks")
            user.purgeData()
        } else {
            println("User ${user.username} is not a valid admin")
        }
    }

    fun demoAdmin() {
        val admin = Admin("adminuser", "admin@myemail.com", listOf("read", "write"))
        attemptAdminTasks(admin)
    }

    // callsInPlace - guarantees that a lambda was invoked in a certain way

    open class Resource {
        fun open() = println("Resource opened")

        fun close() = println("Resource closed")

        fun getValue(): String {
            println("Resource was accessed")
            return "Some string"
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun <R : Resource, A> R.bracket(block: (R) -> A): A {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        this.open()
        try {
            return block(this)
        } finally {
            this.close()
        }
    }

    fun demoResource() {
        val resource = Resource()
        val result: String
        resource.bracket {
            result = it.getValue() // compiler allows this because the contract says this will be run *exactly once*
        }
        println("I got what i wanted: $result")
    }

    class GuardianService {
        init {
            println("Service initialised")
        }

        fun monitorSystem() = println("Monitoring...")
    }

    class GuardianResource {
        private var resource: GuardianService? = null

        @OptIn(ExperimentalContracts::class)
        fun getOrCreate(initializer: () -> GuardianService): GuardianService {
            contract {
                callsInPlace(initializer, InvocationKind.AT_MOST_ONCE)
            }
            if (this.resource == null) {
                this.resource = initializer()
            }
            return this.resource!!
        }
    }

    fun demoGuardian() {
        val guardianResource = GuardianResource()
        val guardianService =
            guardianResource.getOrCreate {
                println("Creating guardian...")
                GuardianService()
            }
        guardianService.monitorSystem()
        val anotherService =
            guardianResource.getOrCreate {
                // this should not run at all, according to the contract
                println("Should not be printed")
                GuardianService()
            }
        anotherService.monitorSystem()
        println(guardianService === anotherService)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoGuardian()
    }
}
