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
@DatabaseTable(tableName = "method")
class MethodElement(): Element {
    override val elementType = Element.Type.METHOD

    @DatabaseField(columnName = "id", generatedId = true)
    override var id: Int = -1

    @DatabaseField(columnName = "name")
    lateinit var name: String

    @DatabaseField(columnName = "desc")
    lateinit var desc: String

    @DatabaseField(columnName = "owner", foreign = true)
    lateinit var owner: TypeElement

    @DatabaseField(columnName = "string_id", unique = true)
    lateinit var stringId: String

    constructor(name: String, desc: String, ownerClass: TypeElement): this() {
        this.stringId = "$name|$desc|${ownerClass.id}"
        this.name = name
        this.desc = desc
        this.owner = ownerClass
        setStringId()
    }

    fun setStringId() {
        this.stringId = "$name|$desc|${owner.id}"
    }

    override fun runtimeId() = "$name|$desc|${owner.runtimeId()}"

    override fun hashCode() = runtimeId().hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is MethodElement) {
            return other.runtimeId() == this.runtimeId()
        }
        return super.equals(other)
    }
}