package com.ivanb.internals

object InlineFunctions {
    data class Product(
        val name: String,
        var price: Double,
    )

    fun List<Product>.applyDiscount(
        discountPercent: Double,
        operation: (Product) -> Unit,
    ) {
        for (product in this) {
            product.price *= (1 - discountPercent / 100)
            operation(product)
        }
    }

    // inlining a function inserts its body (and any lambdas) directly into the compiled code
    inline fun List<Product>.applyDiscountFast(
        discountPercent: Double,
        operation: (Product) -> Unit,
    ) {
        for (product in this) {
            product.price *= (1 - discountPercent / 100)
            operation(product)
        }
    }

    fun demoDiscounts() {
        val products =
            listOf(
                Product("Laptop", 1000.0),
                Product("Phone", 500.0),
                Product("Tablet", 300.0),
                Product("Headphones", 250.0),
            )
        println("Applying a 10% discount...")
        products.applyDiscountFast(10.0) {
            println("${it.name} now costs ${it.price}")
        }
        // the inline call is rewritten to a for loop, with the lambda inlined at every step in the loop
        // this will make the code faster
    }

    fun demoPerf() {
        val products =
            listOf(
                Product("Laptop", 1000.0),
                Product("Phone", 500.0),
                Product("Tablet", 300.0),
                Product("Headphones", 250.0),
            )

        val startNonInline = System.nanoTime()
        repeat(1000) {
            products.applyDiscount(10.0) {
            }
        }
        val durationNonInline = (System.nanoTime() - startNonInline) / 10e9

        val startInline = System.nanoTime()
        repeat(1000) {
            products.applyDiscountFast(10.0) {
            }
        }
        val durationInline = (System.nanoTime() - startInline) / 10e9

        println("Times:")
        println("Non-inline: $durationNonInline")
        println("Inline: $durationInline")
    }

    // sometimes not inlining is useful

    inline fun performOperation(
        noinline storeOperation: () -> Unit, // prevents the function from being inlined, can now access this function as an object
        executeOperation: () -> Unit,
    ) {
        GlobalStore.store(storeOperation)
        executeOperation()
    }

    object GlobalStore {
        private var storedOp: (() -> Unit)? = null

        fun store(op: () -> Unit) {
            storedOp = op
        }

        fun executeStored() {
            storedOp?.invoke()
        }
    }

    fun demoNoInline() {
        performOperation(
            storeOperation = { println("This operation will be called later") },
            executeOperation = { println("This will be called immediately") },
        )

        GlobalStore.executeStored()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        demoNoInline()
    }
}
