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

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Debug: ./gradlew assDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true
 * @author nekocode (nekocode.cn@gmail.com)
 */
class DepanPlugin : Plugin<Project> {

    companion object {
        const val TASK_PREFIX = "transformClassesWithDepanFor"
    }

    override fun apply(project: Project) {
        val androidExtension = project.extensions.getByName("android") as BaseExtension?
        androidExtension ?: return

        // Add depan config to ext
        project.extensions.create("depan", DepanConfig::class.java)

        // Register transform
        androidExtension.registerTransform(DepanTransform(project))

        project.afterEvaluate { _ ->
            project.tasks.forEach { t ->
                // Find all depan transform tasks
                if (t.name.startsWith(TASK_PREFIX)) {
                    val task = t as TransformTask
                    val buildType = task.name.substring(TASK_PREFIX.length)

                    // Always run the task
                    // task.outputs.upToDateWhen { _ -> false }

                    // Pass vars to the task
                    val depanConfig = project.extensions.getByType(DepanConfig::class.java)
                    var dbFile = File(depanConfig.outputDirFile, "$buildType.db")
                    if (!dbFile.isAbsolute) {
                        // Get a absolute file
                        dbFile = project.file(dbFile)
                    }

                    val dbHelper = DbHelper(dbFile)
                    val graphBuilder = GraphBuilder(dbHelper, depanConfig)
                    task.extensions.add("graphBuilder", graphBuilder)
                    task.extensions.add("depanConfig", depanConfig)

                    // Remove old database file
                    task.doFirst {
                        if (dbFile.exists()) {
                            dbFile.delete()
                        }
                    }

                    // After the task is finished, save dependency data to database
                    task.doLast { _ ->
                        if (depanConfig.enabled) {
                            graphBuilder.saveToDb()
                            dbHelper.close()
                            project.logger.quiet("Depan has generated a database file to: ${dbFile.absolutePath}")
                        }
                    }
                }
            }
        }
    }
}
