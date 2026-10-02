package style

import org.openrndr.application

// random idea for later
// dont even need distinction between ids and classes
// we have to try if i can put a vararg classes
// widget("hello-000", "bold-text", ....)

data class Selector(
    val name: String,
    val children: MutableList<Selector> = mutableListOf()
)

class StyleBuilder {
    val entries = mutableListOf<Selector>()

    operator fun String.invoke(block: StyleBuilder.() -> Unit = {}) {
        val childScope = StyleBuilder().apply(block)
        entries += Selector(this, childScope.entries)
    }
}

fun style(block: StyleBuilder.() -> Unit): List<Selector> =
    StyleBuilder().apply(block).entries

fun main() {
    application {
        program {

            style {
                "#" {

                }

                "helloo" {

                }
            }
        }
    }
}