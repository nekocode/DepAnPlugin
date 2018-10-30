/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.gradle.depan.bytecode

import cn.nekocode.gradle.depan.GraphBuilder
import cn.nekocode.gradle.depan.model.FieldElement
import cn.nekocode.gradle.depan.model.MethodElement
import cn.nekocode.gradle.depan.model.TypeElement
import org.objectweb.asm.*

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class DepanClassVisitor(private val graphBuilder: GraphBuilder): ClassVisitor(Opcodes.ASM5) {
    private lateinit var mainType: TypeElement
    private var isSkipped = false

    override fun visit(
            version: Int, access: Int, name: String, signature: String?,
            superName: String?, interfaces: Array<out String>) {
        mainType = TypeElement(name.asmObjectTypeName())
        isSkipped = (graphBuilder.newNode(mainType) == TypeElement.SKIPPED_TYPE)
        if (isSkipped) return

        superName?.asmObjectTypeName()?.let {
            val superClassType = TypeElement(it)
            graphBuilder.newEdge(mainType, superClassType)
        }

        for (i in interfaces) {
            val interfaceType = TypeElement(i.asmObjectTypeName())
            graphBuilder.newEdge(mainType, interfaceType)
        }
    }

    override fun visitAnnotation(
            desc: String, visible: Boolean): AnnotationVisitor? {
        if (isSkipped) return null
        graphBuilder.newEdge(mainType, TypeElement(desc.asmTypeName()))
        return null
    }

    override fun visitTypeAnnotation(
            typeRef: Int, typePath: TypePath?, desc: String, visible: Boolean): AnnotationVisitor? {
        if (isSkipped) return null
        graphBuilder.newEdge(mainType, TypeElement(desc.asmTypeName()))
        return null
    }

    override fun visitField(
            access: Int, name: String, desc: String,
            signature: String?, value: Any?): FieldVisitor? {
        if (isSkipped) return null
        val fieldType = TypeElement(desc.asmTypeName())
        val field = FieldElement(name, fieldType, mainType)
        graphBuilder.newNode(field)
        graphBuilder.newEdge(field, fieldType)
        return DepanFieldVisitor(graphBuilder, field)
    }

    override fun visitMethod(
            access: Int, name: String, desc: String,
            signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (isSkipped) return null
        val method = MethodElement(name, desc, mainType)
        graphBuilder.newNode(method)

        desc.asmArgumentTypes().forEach {
            graphBuilder.newEdge(method, TypeElement(it))
        }

        graphBuilder.newEdge(method,
                TypeElement(desc.asmReturnTypeName()))

        if (exceptions != null) {
            for (exception in exceptions) {
                graphBuilder.newEdge(method,
                        TypeElement(exception.asmObjectTypeName()))
            }
        }

        return DepanMethodVisitor(graphBuilder, method)
    }
}