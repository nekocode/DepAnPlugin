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
import cn.nekocode.gradle.depan.model.Relation
import cn.nekocode.gradle.depan.model.TypeElement
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class DepanFieldVisitor(
        private val graphBuilder: GraphBuilder,
        private val field: FieldElement) : FieldVisitor(Opcodes.ASM5) {

    override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
        graphBuilder.newEdge(field, TypeElement(desc.asmTypeName()), Relation.Type.REFERENCES)
        return null
    }

    override fun visitTypeAnnotation(
            typeRef: Int, typePath: TypePath?, desc: String, visible: Boolean): AnnotationVisitor? {
        graphBuilder.newEdge(field, TypeElement(desc.asmTypeName()), Relation.Type.REFERENCES)
        return null
    }
}