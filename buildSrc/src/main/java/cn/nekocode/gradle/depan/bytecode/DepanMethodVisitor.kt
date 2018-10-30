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
class DepanMethodVisitor(
        private val graphBuilder: GraphBuilder,
        private val method: MethodElement): MethodVisitor(Opcodes.ASM5) {

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
        graphBuilder.newEdge(method,TypeElement(desc.asmTypeName()))
        return null
    }

    override fun visitTypeAnnotation(
            typeRef: Int, typePath: TypePath?, desc: String, visible: Boolean): AnnotationVisitor? {
        graphBuilder.newEdge(method, TypeElement(desc.asmTypeName()))
        return null
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
        val field = FieldElement(
                name,
                TypeElement(desc.asmTypeName()),
                TypeElement(owner.asmObjectTypeName()))
        graphBuilder.newEdge(method, field)
    }

    override fun visitLocalVariable(
            name: String?, desc: String, signature: String?,
            start: Label?, end: Label?, index: Int) {
        graphBuilder.newEdge(method, TypeElement(desc.asmTypeName()))
    }

    override fun visitMethodInsn(
            opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {

        val otherMethod = MethodElement(
                name, desc, TypeElement(owner.asmObjectTypeName()))
        graphBuilder.newEdge(method, otherMethod)

        desc.asmArgumentTypes().forEach {
            graphBuilder.newEdge(otherMethod, TypeElement(it))
        }

        graphBuilder.newEdge(otherMethod,
                TypeElement(desc.asmReturnTypeName()))
    }

    override fun visitMultiANewArrayInsn(desc: String, dims: Int) {
        graphBuilder.newEdge(method, TypeElement(desc.asmTypeName()))
    }

    override fun visitParameterAnnotation(
            parameter: Int, desc: String, visible: Boolean): AnnotationVisitor? {
        graphBuilder.newEdge(method, TypeElement(desc.asmTypeName()))
        return null
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
        if (type != null) {
            graphBuilder.newEdge(method,
                    TypeElement(type.asmObjectTypeName()))
        }
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        graphBuilder.newEdge(method,
                TypeElement(type.asmObjectTypeName()))
    }

    override fun visitLdcInsn(cst: Any?) {
        if (cst is Type) {
            graphBuilder.newEdge(method,
                    TypeElement(getFullyQualifiedTypeName(cst)))
        }
    }
}