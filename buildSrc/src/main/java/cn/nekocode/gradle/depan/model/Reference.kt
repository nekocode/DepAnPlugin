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

    @DatabaseField(columnName = "from_type", dataType = DataType.ENUM_STRING)
    lateinit var fromType: Element.Type

    @DatabaseField(columnName = "from_id")
    var fromId: Int = -1

    @DatabaseField(columnName = "to_type", dataType = DataType.ENUM_STRING)
    lateinit var toType: Element.Type

    @DatabaseField(columnName = "to_id")
    var toId: Int = -1

    @DatabaseField(columnName = "string_id", unique = true)
    lateinit var stringId: String

    private lateinit var fromElement: Element
    private lateinit var toElement: Element

    constructor(fromElement: Element, toElement: Element): this() {
        this.fromElement = fromElement
        this.toElement = toElement
        setStringId()
    }

    fun setStringId() {
        this.fromType = fromElement.elementType
        this.fromId = fromElement.id
        this.toType = toElement.elementType
        this.toId = toElement.id
        this.stringId = "${fromType.ordinal}|$fromId|${toType.ordinal}|$toId"
    }

    fun key() = "${fromElement.runtimeId()}|${toElement.runtimeId()}"

    override fun hashCode() = key().hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is Reference) {
            return other.key() == this.key()
        }
        return super.equals(other)
    }
}