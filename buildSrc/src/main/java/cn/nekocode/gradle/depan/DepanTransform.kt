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

import cn.nekocode.gradle.depan.bytecode.DepanClassVisitor
import com.android.SdkConstants
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.utils.FileUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class DepanTransform(private val project: Project) : Transform() {

    override fun getName() = "depan"

    override fun getInputTypes(): Set<QualifiedContent.ContentType>
            = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>
            = TransformManager.EMPTY_SCOPES

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope>
            = TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental() = false

    override fun transform(invocation: TransformInvocation) {
        val ext = (invocation.context as TransformTask).extensions
        val config = ext.getByType(DepanConfig::class.java)
        if (!config.enabled) {
            return
        }
        val graphBuilder = ext.getByType(GraphBuilder::class.java)

        invocation.referencedInputs.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                for (file in FileUtils.getAllFiles(directoryInput.file)) {
                    if (!file.name.endsWith(SdkConstants.DOT_CLASS)) {
                        continue
                    }

                    file.inputStream().use {
                        transform(it, graphBuilder)
                    }
                }
            }

            input.jarInputs.forEach { jarInput ->
                jarInput.file.inputStream().use { jis ->
                    ZipInputStream(jis).use { zis ->
                        var entry: ZipEntry? = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory &&
                                    entry.name.endsWith(SdkConstants.DOT_CLASS)) {
                                transform(zis, graphBuilder)
                            }

                            entry = zis.nextEntry
                        }
                    }
                }
            }
        }
    }

    private fun transform(ins: InputStream, graphBuilder: GraphBuilder) {
        val visitor = DepanClassVisitor(project, graphBuilder)

        try {
            val cr = ClassReader(ins)
            cr.accept(visitor, 0)

        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }
}