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

package cn.nekocode.gradle.depan

import cn.nekocode.gradle.depan.model.*

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class GraphBuilder(
        private val dbHelper: DbHelper,
        private val depanConfig: DepanConfig) {

    companion object {
        const val SAVE_THRESHOLD = 10000
    }

    private val typeNodes = HashMap<String, TypeElement>()
    private val nodes = HashMap<String, Element>()
    private val edges = HashSet<Reference>()

    fun newNode(element: Element): Element {
        // Save to memory
        val rlt: Element = when (element) {
            is TypeElement -> {
                if (depanConfig.typeFilter.invoke(element.name)) {
                    typeNodes.getOrPut(element.runtimeId(), element)
                } else {
                    return TypeElement.SKIPPED_TYPE
                }
            }
            is FieldElement -> {
                var e = typeNodes[element.owner.runtimeId()]
                if (e != null) {
                    element.owner = e
                } else {
                    e = newNode(element.owner) as TypeElement?
                    element.owner = e ?: TypeElement.SKIPPED_TYPE
                }

                e = typeNodes[element.type.runtimeId()]
                if (e != null) {
                    element.type = e
                } else {
                    e = newNode(element.type) as TypeElement?
                    element.type = e ?: TypeElement.SKIPPED_TYPE
                }

                nodes.getOrPut(element.runtimeId(), element)
            }
            is MethodElement -> {
                var e = typeNodes[element.owner.runtimeId()]
                if (e != null) {
                    element.owner = e
                } else {
                    e = newNode(element.owner) as TypeElement?
                    element.owner = e ?: TypeElement.SKIPPED_TYPE
                }

                nodes.getOrPut(element.runtimeId(), element)
            }
            else -> {
                throw Exception()
            }
        }

        if (nodes.size > SAVE_THRESHOLD) {
            // If there are too many data, save them to db
            saveToDb()
        }
        return rlt
    }

    fun newEdge(fromElement: Element, toElement: Element, relation: Relation): Reference? {
        // Save to memory
        val reference = Reference(newNode(fromElement), newNode(toElement), relation)
        edges.add(reference)

        if (edges.size > SAVE_THRESHOLD) {
            // If there are too many edges, auto save to db
            saveToDb()
        }
        return reference
    }

    fun saveToDb() {
        dbHelper.callInTransaction {
            typeNodes.values.forEach {
                val dao = dbHelper.typeElementDao
                val old = dao.queryBuilder().where()
                        .eq("name", it.name)
                        .queryForFirst()
                if (old != null) {
                    it.id = old.id
                    if ((it.accessFlags != Element.ACCESS_FLAGS_MISSING) and
                            (old.accessFlags == Element.ACCESS_FLAGS_MISSING)) {
                        dao.updateBuilder().run {
                            updateColumnValue("access_flags", it.accessFlags)
                            where().eq("id", old.id)
                            update()
                        }
                    }
                } else {
                    dao.create(it)
                }
            }

            nodes.values.forEach {
                when (it) {
                    is FieldElement -> {
                        it.updateStringId()
                        val dao = dbHelper.fieldElementDao
                        val old = dao.queryBuilder().where()
                                .eq("string_id", it.stringId)
                                .queryForFirst()
                        if (old != null) {
                            it.id = old.id
                            if ((it.accessFlags != Element.ACCESS_FLAGS_MISSING) and
                                    (old.accessFlags == Element.ACCESS_FLAGS_MISSING)) {
                                dao.updateBuilder().run {
                                    updateColumnValue("access_flags", it.accessFlags)
                                    where().eq("id", old.id)
                                    update()
                                }
                            }
                        } else {
                            dao.create(it)
                        }
                    }
                    is MethodElement -> {
                        it.updateStringId()
                        val dao = dbHelper.methodElementDao
                        val old = dao.queryBuilder().where()
                                .eq("string_id", it.stringId)
                                .queryForFirst()
                        if (old != null) {
                            it.id = old.id
                            if ((it.accessFlags != Element.ACCESS_FLAGS_MISSING) and
                                    (old.accessFlags == Element.ACCESS_FLAGS_MISSING)) {
                                dao.updateBuilder().run {
                                    updateColumnValue("access_flags", it.accessFlags)
                                    where().eq("id", old.id)
                                    update()
                                }
                            }
                        } else {
                            dao.create(it)
                        }
                    }
                }
            }

            edges.forEach {
                it.updateStringId()
                val dao = dbHelper.referenceDao
                val old = dao.queryBuilder().where()
                        .eq("string_id", it.stringId)
                        .queryForFirst()
                if (old == null) {
                    dao.create(it)
                }
            }
            null
        }

        // Clear memory cache
        nodes.clear()
        edges.clear()
    }

    private fun <T: Element> HashMap<String, T>.getOrPut(id: String, defaultElement: T): T {
        val oldElement = this[id]
        if (oldElement == null) {
            this[id] = defaultElement
            return defaultElement
        }

        if (oldElement.accessFlags == Element.ACCESS_FLAGS_MISSING) {
            oldElement.accessFlags = defaultElement.accessFlags
        }

        return oldElement
    }
}