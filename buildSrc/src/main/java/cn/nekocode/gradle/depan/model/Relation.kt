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

package cn.nekocode.gradle.depan.model

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
enum class Relation(vararg descriptions: String) {
    // Type to Type
    E("Extends", "Implements"),

    // Field/Method to Type
    T("Type is", "Type references"),

    // Method to Field/Method
    C("Calls a method", "Uses a field"),

    // Type/Field/Method to Type
    A("Is annotated with"),
}