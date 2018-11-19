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

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
@DatabaseTable(tableName = "reference")
class Reference() {
    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int = -1

    @DatabaseField(columnName = "from_sort", dataType = DataType.ENUM_INTEGER)
    lateinit var fromSort: ElementSort

    @DatabaseField(columnName = "from_id")
    var fromId: Int = -1

    @DatabaseField(columnName = "to_sort", dataType = DataType.ENUM_INTEGER)
    lateinit var toSort: ElementSort

    @DatabaseField(columnName = "to_id")
    var toId: Int = -1

    @DatabaseField(columnName = "relation", dataType = DataType.ENUM_INTEGER)
    lateinit var relation: Relation

    @DatabaseField(columnName = "string_id", unique = true)
    lateinit var stringId: String

    private lateinit var fromElement: Element
    private lateinit var toElement: Element

    constructor(fromElement: Element, toElement: Element, relation: Relation) : this() {
        this.fromElement = fromElement
        this.toElement = toElement
        this.relation = relation
        updateStringId()
    }

    fun updateStringId() {
        this.fromSort = fromElement.elementSort
        this.fromId = fromElement.id
        this.toSort = toElement.elementSort
        this.toId = toElement.id
        this.stringId = "${fromSort.ordinal}|$fromId|${toSort.ordinal}|$toId|${relation.ordinal}"
    }

    fun runtimeId() = "${fromElement.runtimeId()}|${toElement.runtimeId()}|${relation.ordinal}"

    override fun hashCode() = runtimeId().hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is Reference) {
            return other.runtimeId() == this.runtimeId()
        }
        return super.equals(other)
    }
}