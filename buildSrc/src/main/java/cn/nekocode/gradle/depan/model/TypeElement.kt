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

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
@DatabaseTable(tableName = "type")
class TypeElement() : Element {
    companion object {
        val SKIPPED_TYPE = TypeElement("SKIPPED_TYPE")
    }

    override val elementSort = ElementSort.T

    @DatabaseField(columnName = "id", generatedId = true)
    override var id: Int = -1

    @DatabaseField(columnName = "name", unique = true)
    lateinit var name: String

    constructor(name: String) : this() {
        this.name = name
    }

    override fun runtimeId() = name

    override fun hashCode() = runtimeId().hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is TypeElement) {
            return other.runtimeId() == this.runtimeId()
        }
        return super.equals(other)
    }
}