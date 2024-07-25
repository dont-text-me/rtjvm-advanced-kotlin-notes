package com.ivanb.builderksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class BuilderProcessor(
    private val generator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(BUILDER_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .forEach(this::generateBuilderClass)

        // return only the symbols that CANNOT be processed at this time i.e. none in this case
        return emptyList()
    }

    private fun generateBuilderClass(clazz: KSClassDeclaration) {
        val className = clazz.simpleName.asString()
        val packageName = clazz.packageName.asString()
        val properties = clazz.getAllProperties().toList()
        val originalFiles = listOfNotNull(clazz.containingFile).toTypedArray()
        val builderClassName = "${className}Builder"
        val file =
            generator.createNewFile(
                Dependencies(false, *originalFiles),
                packageName,
                builderClassName,
            )
        // note: the whitespace gets a bit mangled but can be fixed with the spotless plugin
        file.bufferedWriter().use {
            it.write(
                """
                package $packageName
                
                class $builderClassName {
                    ${
                    properties.joinToString("\n") { prop ->
                        val propertyName = prop.simpleName.asString()
                        val propertyType =
                            prop.type.resolve() // can crash if wildcard types are used in the annotated class
                        """
                        private var $propertyName : $propertyType? = null
                        fun $propertyName(value: $propertyType) = apply {this.$propertyName = value}
                        """
                    }
                }
                
                fun build() : $className = $className(
                        ${
                    properties.joinToString("\n") { prop ->
                        """${prop.simpleName.asString()} ?: throw IllegalArgumentException("${prop.simpleName.asString()} must be provided"),"""
                    }
                }
                    )
                }
                """,
            )
        }
    }

    companion object {
        const val BUILDER_ANNOTATION_NAME = "com.ivanb.builderksp.Builder"
    }
}
