package com.movtery.zalithlauncher.utils.file

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 打包目录为 zip 文件（M1 自 FileUtils 拆出：纯 JDK 实现，供核心库与宿主共用）
 */
suspend fun zipDirectory(
    sourceDir: File,
    outputZipFile: File,
    preserveFileTime: Boolean = true
) = withContext(Dispatchers.IO) {
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        throw IllegalArgumentException("Source path must be an existing directory")
    }

    ZipOutputStream(FileOutputStream(outputZipFile)).use { zipOut ->
        sourceDir.walkTopDown().filter { it.isFile }.forEach { file ->
            val entryName = file.relativeTo(sourceDir).path.replace("\\", "/")
            val zipEntry = ZipEntry(entryName)
            if (preserveFileTime) {
                zipEntry.time = file.lastModified()
            }
            zipOut.putNextEntry(zipEntry)
            file.inputStream().use { input ->
                input.copyTo(zipOut)
            }
            zipOut.closeEntry()
        }
    }
}
