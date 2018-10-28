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
import cn.nekocode.gradle.depan.model.Relation
import cn.nekocode.gradle.depan.model.TypeElement
import org.gradle.api.Project
import org.objectweb.asm.*

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class DepanClassVisitor(
        private val project: Project,
        private val graphBuilder: GraphBuilder): ClassVisitor(Opcodes.ASM5) {

    companion object {
        const val CLASS_NAME_PREFIX = "class\$"
    }
    private lateinit var mainClass: TypeElement
    private var skipClass = false

    override fun visit(
            version: Int, access: Int, name: String, signature: String?,
            superName: String?, interfaces: Array<out String>) {
        mainClass = TypeElement(name.asmObjectTypeName())
        skipClass = (graphBuilder.newNode(mainClass) == TypeElement.SKIPPED_TYPE)
        if (skipClass) return

        superName?.asmObjectTypeName()?.let {
            graphBuilder.newEdge(mainClass, TypeElement(it), Relation.Type.REFERENCES)
        }

        for (i in interfaces) {
            graphBuilder.newEdge(mainClass,
                    TypeElement(i.asmObjectTypeName()), Relation.Type.REFERENCES)
        }
    }

    override fun visitAnnotation(
            desc: String, visible: Boolean): AnnotationVisitor? {
        if (skipClass) return null

        graphBuilder.newEdge(mainClass, TypeElement(desc.asmTypeName()), Relation.Type.REFERENCES)
        return null
    }

    override fun visitField(
            access: Int, name: String, desc: String,
            signature: String?, value: Any?): FieldVisitor? {
        if (skipClass) return null

        val field = FieldElement(name, TypeElement(desc.asmTypeName()), mainClass)
        graphBuilder.newNode(field)
        graphBuilder.newEdge(mainClass, field, Relation.Type.HAS)

        if ((access.and(Opcodes.ACC_SYNTHETIC)) == Opcodes.ACC_SYNTHETIC) {
            if (Class::class.java.name == Type.getType(desc).className) {
                if (name.startsWith(CLASS_NAME_PREFIX)) {
                    var realName = name.substring(CLASS_NAME_PREFIX.length)
                    realName = realName.replace('$', '.')
                    val lastDotIndex = realName.lastIndexOf('.')

                    for (i in 0..(lastDotIndex - 1)) {
                        if (Character.isUpperCase(realName[i])) {
                            if (i == 0) {
                                return null
                            }
                            if (realName[i - 1] == '.') {
                                realName = realName.substring(0, i) +
                                        realName.substring(i).replace('.', '$')
                                break
                            }
                        }
                    }
                    if (Character.isJavaIdentifierStart(realName[0])) {
                        graphBuilder.newEdge(field, TypeElement(realName), Relation.Type.REFERENCES)
                    }
                }
            }
        }

        return DepanFieldVisitor(project, graphBuilder, field)
    }

    override fun visitMethod(
            access: Int, name: String, desc: String,
            signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (skipClass) return null

        val method = MethodElement(name, desc, mainClass)
        graphBuilder.newNode(method)
        graphBuilder.newEdge(mainClass, method, Relation.Type.HAS)

        desc.asmArgumentTypes().forEach {
            graphBuilder.newEdge(method, TypeElement(it), Relation.Type.REFERENCES)
        }

        graphBuilder.newEdge(method,
                TypeElement(desc.asmReturnTypeName()), Relation.Type.REFERENCES)

        if (exceptions != null) {
            for (exception in exceptions) {
                graphBuilder.newEdge(method,
                        TypeElement(exception.asmObjectTypeName()), Relation.Type.REFERENCES)
            }
        }

        return DepanMethodVisitor(project, graphBuilder, method)
    }
}