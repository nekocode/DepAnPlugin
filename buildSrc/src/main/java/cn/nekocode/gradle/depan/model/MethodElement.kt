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
    override lateinit var key: String

    @DatabaseField(columnName = "id", generatedId = true)
    override var id: Int = -1

    @DatabaseField(columnName = "name")
    lateinit var name: String

    @DatabaseField(columnName = "desc")
    lateinit var desc: String

    @DatabaseField(columnName = "owner_type", foreign = true)
    lateinit var ownerType: TypeElement

    @DatabaseField(columnName = "_key", unique = true)
    lateinit var _key: String

    constructor(name: String, desc: String, ownerClass: TypeElement): this() {
        this.key = "$name|$desc|${ownerClass.key}"
        this._key = "$name|$desc|${ownerClass.id}"
        this.name = name
        this.desc = desc
        this.ownerType = ownerClass
    }

    fun update() {
        this._key = "$name|$desc|${ownerType.id}"
    }

    override fun hashCode() = key.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other is MethodElement) {
            return other.key == this.key
        }
        return super.equals(other)
    }
}