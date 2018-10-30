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
import com.google.common.io.Files
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.misc.TransactionManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import java.io.File
import java.sql.SQLException

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class DbHelper(dbFile: File) {
    private val connectionSource by lazy {
        Files.createParentDirs(dbFile)
        val rlt = JdbcConnectionSource("jdbc:sqlite:${dbFile.path}")
        onCreate(rlt)
        rlt
    }
    val typeElementDao: Dao<TypeElement, *> by lazy {
        val rlt = DaoManager.createDao(connectionSource, TypeElement::class.java)
        TypeElement.SKIPPED_TYPE = rlt.createIfNotExists(TypeElement.SKIPPED_TYPE)
        rlt
    }
    val fieldElementDao: Dao<FieldElement, *> by lazy {
        DaoManager.createDao(connectionSource, FieldElement::class.java)
    }
    val methodElementDao: Dao<MethodElement, *> by lazy {
        DaoManager.createDao(connectionSource, MethodElement::class.java)
    }
    val referenceDao: Dao<Reference, *> by lazy {
        DaoManager.createDao(connectionSource, Reference::class.java)
    }

    private fun onCreate(connectionSource: ConnectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, TypeElement::class.java)
            TableUtils.createTableIfNotExists(connectionSource, FieldElement::class.java)
            TableUtils.createTableIfNotExists(connectionSource, MethodElement::class.java)
            TableUtils.createTableIfNotExists(connectionSource, Reference::class.java)
        } catch (ignored: SQLException) {
        }
    }

    fun close() {
        connectionSource.close()
    }

    fun <T> callInTransaction(callable: () -> T): T {
        return TransactionManager.callInTransaction(connectionSource, callable)
    }
}