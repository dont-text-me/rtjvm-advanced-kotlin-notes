package com.ivanb.practice

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter

object KotlinTags {
    sealed interface HTMLElement

    data class Html(
        val head: Head,
        val body: Body,
    ) : HTMLElement {
        override fun toString(): String = "<html>\n$head\n$body\n</html>"
    }

    data class Body(
        val content: List<HTMLElement>,
    ) : HTMLElement {
        override fun toString(): String = "<body>\n${content.joinToString("\n")}\n</body>"
    }

    data class Div(
        val id: String? = null,
        val className: String? = null,
        val content: List<HTMLElement>,
    ) : HTMLElement {
        override fun toString(): String =
            "<div ${id?.let{"id = \"$it\""} ?: ""} ${className?.let{"class= \"$it\""} ?: ""}>\n" +
                content.joinToString("\n") +
                "\n</div>"
    }

    data class P(
        val text: String,
    ) : HTMLElement {
        override fun toString(): String = "<p>$text</p>"
    }

    data class Title(
        val text: String,
    ) : HTMLElement {
        override fun toString(): String = "<title>$text</title>"
    }

    data class Head(
        val content: List<HTMLElement>,
    ) : HTMLElement {
        override fun toString(): String = "<head>\n${content.joinToString("\n")}\n</head>"
    }

    class DivBuilder(
        private val id: String?,
        private val className: String?,
    ) {
        private val children = mutableListOf<HTMLElement>()

        fun p(content: String) = children.add(P(content))

        fun build() = Div(id = id, className = className, content = children)
    }

    class HtmlBuilder {
        private lateinit var head: Head
        private lateinit var body: Body

        fun head(init: HeadBuilder.() -> Unit) {
            val builder = HeadBuilder()
            builder.init()
            head = builder.build()
        }

        fun body(init: BodyBuilder.() -> Unit) {
            val builder = BodyBuilder()
            builder.init()
            body = builder.build()
        }

        fun build() = Html(head, body)
    }

    class HeadBuilder {
        private val children = mutableListOf<HTMLElement>()

        fun title(content: String) = children.add(Title(content))

        fun build() = Head(children)
    }

    class BodyBuilder {
        private val children = mutableListOf<HTMLElement>()

        fun div(
            id: String?,
            className: String?,
            init: DivBuilder.() -> Unit,
        ) {
            val builder = DivBuilder(id, className)
            builder.init()
            children.add(builder.build())
        }

        fun p(content: String) = children.add(P(content))

        fun build() = Body(children)
    }

    fun html(init: HtmlBuilder.() -> Unit): Html {
        val builder = HtmlBuilder()
        builder.init()
        return builder.build()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val document =
            html {
                head {
                    title("My title!")
                }
                body {
                    div(id = "Myid", className = "myClass") {
                        p("First paragraph")
                        p("Second paragraph")
                    }
                }
            }

        PrintWriter(FileWriter(File("src/main/resources/out.html"))).use {
            println(document)
            it.println(document)
        }
    }
}
