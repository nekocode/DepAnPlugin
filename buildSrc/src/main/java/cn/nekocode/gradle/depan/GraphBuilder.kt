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
    private val typeNodes = HashMap<String, TypeElement>()
    private val nodes = HashMap<String, Element>()
    private val edges = HashSet<Relation>()

    fun newNode(element: Element): Element {
        // Save to memory
        val rlt: Element = when (element) {
            is TypeElement -> {
                if (depanConfig.typeFilter.invoke(element.name)) {
                    typeNodes.getOrPut(element.key) { element }
                } else {
                    return TypeElement.SKIPPED_TYPE
                }
            }
            is FieldElement -> {
                var e = typeNodes[element.ownerClass.key]
                if (e != null) {
                    element.ownerClass = e
                } else {
                    e = newNode(element.ownerClass) as TypeElement?
                    element.ownerClass = e ?: TypeElement.SKIPPED_TYPE
                }

                e = typeNodes[element.type.key]
                if (e != null) {
                    element.type = e
                } else {
                    e = newNode(element.type) as TypeElement?
                    element.type = e ?: TypeElement.SKIPPED_TYPE
                }

                nodes.getOrPut(element.key) { element }
            }
            is MethodElement -> {
                var e = typeNodes[element.ownerClass.key]
                if (e != null) {
                    element.ownerClass = e
                } else {
                    e = newNode(element.ownerClass) as TypeElement?
                    element.ownerClass = e ?: TypeElement.SKIPPED_TYPE
                }

                nodes.getOrPut(element.key) { element }
            }
            else -> {
                throw Exception()
            }
        }

        if (nodes.size > 5000) {
            // If there are too many data, save them to db
            saveToDb()
        }
        return rlt
    }

    fun newEdge(fromElement: Element, toElement: Element, type: Relation.Type): Relation? {
        // Save to memory
        val relation = Relation(newNode(fromElement), newNode(toElement), type)
        edges.add(relation)

        if (edges.size > 5000) {
            // If there are too many edges, auto save to db
            saveToDb()
        }
        return relation
    }

    fun saveToDb() {
        dbHelper.callInTransaction {
            typeNodes.values.forEach {
                val dao = dbHelper.typeElementDao
                val rlt = dao.queryBuilder().where()
                        .eq("name", it.name)
                        .queryForFirst()
                if (rlt != null) {
                    it.id = rlt.id
                } else {
                    dao.create(it)
                }
            }

            nodes.values.forEach {
                when (it) {
                    is FieldElement -> {
                        it.update()
                        val dao = dbHelper.fieldElementDao
                        val rlt = dao.queryBuilder().where()
                                .eq("_key", it._key)
                                .queryForFirst()
                        if (rlt != null) {
                            it.id = rlt.id
                        } else {
                            dao.create(it)
                        }
                    }
                    is MethodElement -> {
                        it.update()
                        val dao = dbHelper.methodElementDao
                        val rlt = dao.queryBuilder().where()
                                .eq("_key", it._key)
                                .queryForFirst()
                        if (rlt != null) {
                            it.id = rlt.id
                        } else {
                            dao.create(it)
                        }
                    }
                }
            }

            edges.forEach {
                it.update()
                val dao = dbHelper.relationDao
                val rlt = dao.queryBuilder().where()
                        .eq("_key", it._key)
                        .queryForFirst()
                if (rlt == null) {
                    dao.create(it)
                }
            }
            null
        }

        // Clear memory cache
        nodes.clear()
        edges.clear()
    }
}