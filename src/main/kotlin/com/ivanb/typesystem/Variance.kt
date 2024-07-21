package com.ivanb.typesystem

/**
 * # Variance lecture notes
 *
 * ## If A extends B, should Thing<A> extend Thing<B>?
 *
 * - yes - covariant
 * - no - invariant
 * - no, the opposite - contravariant
 *
 * ## Variance rules (from lecture):
 *
 * - if it "outputs" elements -> covariant (out)
 * - if it "consumes" elements -> contravariant (in)
 * - otherwise -> invariant (no modifier)
 *
 * - types of vars are in "in" position -> must be invariant
 * - method arg types are in "in" position -> must be contravariant
 *      - cannot use covariant types in method args
 * - method return types are in "out" position -> must be covariant
 * */
object Variance {
    class RandomGenerator<out A>

    class MyOption<out A>

    class JSONSerializer<in A>

    interface MyFunction<in A, out B>

    sealed class Animal

    class Dog : Animal()

    class Cat : Animal()

    abstract class LList<out A> {
        abstract fun head(): A

        abstract fun tail(): LList<A>
    }

    data object EmptyList : LList<Nothing>() {
        override fun head(): Nothing = throw NoSuchElementException()

        override fun tail(): LList<Nothing> = throw NoSuchElementException()
    }

    data class Cons<out A>(
        val h: A,
        val t: LList<A>,
    ) : LList<A>() {
        override fun head(): A = h

        override fun tail(): LList<A> = t
    }

    fun <B, A : B> LList<A>.add(elem: B): LList<B> = Cons(elem, this)

    abstract class Vehicle

    open class Car : Vehicle()

    class Supercar : Car()

    class RepairShop<in A : Vehicle> {
        fun <B : A> repair(elem: B): B = elem
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val myList: LList<Dog> = EmptyList
        val dogs = myList.add(Dog()).add(Dog())
        val animals = dogs.add(Cat())

        val shop = RepairShop<Vehicle>()
        val repairedSupercar: Supercar = shop.repair(Supercar())
        val repairedCar: Car = shop.repair(Car())
    }
}
