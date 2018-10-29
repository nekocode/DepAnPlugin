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
@DatabaseTable(tableName = "relation")
class Relation() {
    enum class Type {
        HAS, REFERENCES,
    }
    lateinit var key: String

    @DatabaseField(columnName = "id", generatedId = true)
    var id: Int = -1

    @DatabaseField(columnName = "fromType", dataType = DataType.ENUM_STRING)
    lateinit var fromType: Element.Type

    @DatabaseField(columnName = "fromId")
    var fromId: Int = -1

    @DatabaseField(columnName = "toType", dataType = DataType.ENUM_STRING)
    lateinit var toType: Element.Type

    @DatabaseField(columnName = "toId")
    var toId: Int = -1

    @DatabaseField(columnName = "relation", dataType = DataType.ENUM_STRING)
    lateinit var relation: Type

    @DatabaseField(columnName = "_key", unique = true)
    lateinit var _key: String

    private lateinit var fromElement: Element
    private lateinit var toElement: Element

    constructor(fromElement: Element, toElement: Element, relation: Type): this() {
        this.relation = relation
        this.fromElement = fromElement
        this.toElement = toElement
        update()
    }

    fun update() {
        this.fromType = fromElement.elementType
        this.fromId = fromElement.id
        this.toType = toElement.elementType
        this.toId = toElement.id
        this.key = "${fromElement.key}|${toElement.key}|$relation"
        this._key = "${fromType.ordinal}|$fromId|" +
                "${toType.ordinal}|$toId|${relation.ordinal}"
    }

    override fun hashCode() = key.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is Relation) {
            return other.key == this.key
        }
        return super.equals(other)
    }
}