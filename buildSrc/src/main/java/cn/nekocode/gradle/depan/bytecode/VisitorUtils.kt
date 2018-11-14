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

import org.objectweb.asm.Type

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
fun getFullyQualifiedTypeName(type: Type): String {
    return when (type.sort) {
        Type.VOID -> "void"
        Type.BOOLEAN -> "boolean"
        Type.CHAR -> "char"
        Type.BYTE -> "byte"
        Type.SHORT -> "short"
        Type.INT -> "int"
        Type.FLOAT -> "float"
        Type.LONG -> "long"
        Type.DOUBLE -> "double"
        Type.ARRAY -> getFullyQualifiedTypeName(type.elementType)
        Type.OBJECT -> type.className
        Type.METHOD -> "method"
        else -> "unknown"
    }
}

fun String.asmObjectTypeName() =
        getFullyQualifiedTypeName(Type.getObjectType(this))

fun String.asmTypeName() =
        getFullyQualifiedTypeName(Type.getType(this))

fun String.asmArgumentTypes(): List<String> =
        Type.getArgumentTypes(this)
                .map { getFullyQualifiedTypeName(it) }

fun String.asmReturnTypeName() =
        getFullyQualifiedTypeName(Type.getReturnType(this))