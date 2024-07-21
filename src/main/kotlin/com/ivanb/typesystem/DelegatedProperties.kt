package com.ivanb.typesystem

import java.util.UUID
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.reflect.KProperty

object DelegatedProperties {
    class LoggingClassN(
        val id: Int,
    ) {
        var property: Int = 0
            get() {
                println("[logging $id] getting property")
                return field
            }
            set(value) {
                println("[logging $id] setting property to new value $value")
                field = value
            }
    }

    fun demoNaiveLogger() {
        val logger = LoggingClassN(42)
        logger.property = 2
        val x = logger.property
        println(x)
        logger.property = 3
        println(logger.property)
    }

    class Dataset(
        val name: String,
    ) {
        private var state: String = "not loaded" // loggable
        private var data: List<String> = listOf() // loggable
        private var size: Int = 0
    }

    class LoggingProp<A>(
        val id: String,
        val default: A,
    ) {
        var property: A = default

        operator fun getValue(
            currentRef: Any,
            prop: KProperty<*>,
        ): A {
            println("[logging $id] getting property")
            return property
        }

        operator fun setValue(
            currentRef: Any,
            prop: KProperty<*>,
            value: A,
        ) {
            println("[logging $id] setting property to new value $value")
            property = value
        }
    }

    class LoggingClass(
        id: Int,
    ) {
        var property: Int by LoggingProp<Int>("$id-firstProperty", 0)
        var secondProperty: Int by LoggingProp<Int>("$id-secondProperty", 0)
        var stringProperty: String by LoggingProp<String>("$id-stringProperty", "")
    }

    fun demoLogger() {
        val loggingClass = LoggingClass(42)
        loggingClass.property = 32
        val x = loggingClass.property
        loggingClass.secondProperty = 23
        val y = loggingClass.secondProperty + x
        val z = loggingClass.stringProperty
    }

    class Delayed<A>(
        private val func: () -> A,
    ) {
        private var content: A? = null

        operator fun getValue(
            currentRef: Any,
            prop: KProperty<*>,
        ): A {
            if (content == null) {
                content = func()
            }
            return content!!
        }
    }

    class DelayedClass {
        val delayedProp: Int by Delayed {
            println("Running func")
            42
        }
    }

    /**
     * Lazy evaluation - variable is not set until first use
     * */
    fun demoDelayed() {
        val delayed = DelayedClass()
        val x = delayed.delayedProp // will log
        val y = delayed.delayedProp // won't log, already initialised
    }

    data class UserData(
        val name: String,
        val email: String,
    )

    class Person(
        val id: String,
    ) {
        private fun fetchUserData(): UserData {
            println("Running long operation...")
            Thread.sleep(3000)
            return UserData(name = "Some user", email = "someuser@google.com")
        }

        val userData: UserData by lazy {
            fetchUserData()
        }
    }

    fun demoLazy() {
        val person = Person(id = "1234")
        println("user created")
        println(person.userData)
        println("accessing again")
        println(person.userData)
    }

    // vetoable

    class BankAccount(
        initialBalance: Double,
    ) {
        var balance: Double by Delegates.vetoable(initialBalance) { prop, oldValue, newValue ->
            // must return a boolean, if true, the variable will change, if not, it will be denied
            newValue >= 0
        }
    }

    fun demoVeto() {
        val account = BankAccount(100.0)
        println("Initial balance - ${account.balance}")
        account.balance = 150.0
        println("New balance - ${account.balance}")
        account.balance = -10.0
        println("New balance - ${account.balance}") // will be vetoed, balance will not change
    }

    // observable - perform side effects on property changes

    enum class State {
        NONE,
        NEW,
        PROCESSED,
        STALE,
    }

    class MonitoredDataset(
        name: String,
    ) {
        var state: State by Delegates.observable(State.NONE) { property, oldValue, newValue ->
            println("[dataset - $name] State changed from $oldValue to $newValue ")
            if (newValue == State.STALE) {
                println("[dataset - $name] dataset is stale!")
            }
        }
        private var data: List<String> = emptyList()

        fun consumeData() {
            if (state == State.PROCESSED) {
                state = State.STALE
            } else if (data.isNotEmpty()) {
                state = State.PROCESSED
                data = emptyList()
            }
        }

        fun fetchData() {
            if (Random.nextBoolean()) {
                data = (1..5).map { UUID.randomUUID().toString() }
                state = State.NEW
            }
        }
    }

    fun demoObservable() {
        val dataset = MonitoredDataset("sensor-data-incremental")
        dataset.fetchData()
        dataset.consumeData()
        dataset.fetchData()
        repeat(10) { dataset.consumeData() }
    }

    class WeakObject(
        val attributes: Map<String, Any>,
    ) {
        val name: String by attributes
        val size: Int by attributes // will crash if "size" is not a key
    }

    fun demoWeak() {
        val myObj =
            WeakObject(
                mapOf(
                    "size" to 123456,
                    "name" to "my object",
                ),
            )
        println(myObj.size)
        println(myObj.name)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoWeak()
    }
}
