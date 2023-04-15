package utils

import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.LogManager
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.function.Consumer

object FileSupporter {
    private val logger = LogManager.getLogger()

    fun authorize(path: Path, isDirectory: Boolean) {
        try {
            if (Files.exists(path)) {
                return
            }
            if (isDirectory) {
                Files.createDirectories(path)
            } else {
                val parent: Path = path.parent
                if (!Files.exists(parent)) {
                    Files.createDirectories(parent)
                }
                Files.createFile(path)
            }
        } catch (e: Exception) {
            logger.error("cannot authorize {} ", path, e)
        }
    }

    fun tempFile(): Path? {
        return try {
            Files.createTempFile("fcm_service_", "")
        } catch (e: Exception) {
            logger.error("cannot create temp file ", e)
            null
        }
    }

    fun tempDirectory(): Path? {
        return try {
            Files.createTempDirectory("fcm_service_")
        } catch (e: Exception) {
            logger.error("cannot create temp directory ", e)
            null
        }
    }

    fun fileMirror(source: Path, prefix: String): Path {
        val des: Path = Files.createTempFile(prefix, "HNC")
        Files.copy(source, des, StandardCopyOption.REPLACE_EXISTING)
        return des
    }

    fun write(file: Path, text: String, vararg options: OpenOption) {
        try {
            authorize(file.parent, true)
            Files.write(file, text.toByteArray(StandardCharsets.UTF_8), *options)
        } catch (e: java.lang.Exception) {
            logger.error("error to write {} to {} by ", text, file, e)
        }
    }

    fun readLines(file: Path, cons: Consumer<String>) {
        try {
            Files.lines(file).use { lines -> lines.forEach(cons) }
        } catch (e: java.lang.Exception) {
            logger.error("cannot do line by line with {}", file, e)
        }
    }

    fun doWithChildren(dir: Path, cons: (Path) -> Unit) {
        try {
            Files.walk(dir, 1)
                .filter { p: Path -> p != dir }
                .use { children -> children.forEach(cons) }
        } catch (e: java.lang.Exception) {
            logger.warn("cannot do with children of {}", dir, e)
        }
    }

    fun findDescendant(dir: Path, filename: String): Path? {
        try {
            Files.walk(dir, Int.MAX_VALUE)
                .use { descendants ->
                    return descendants.filter { it.fileName.toString() == filename }.findAny().get()
                }
        }
        catch (e: Exception) {
            logger.warn("cannot find descendant {} of {}", filename, dir, e)
            return null
        }
    }

    fun deleteIfExists(input: Path?): Boolean {
        return try {
            if (input == null || !Files.exists(input)) {
                return true
            }
            FileUtils.forceDelete(input.toFile())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyFile(inputFile: Path, outputFile: Path): Boolean {
        return try {
            FileUtils.copyFile(inputFile.toFile(), outputFile.toFile())
            true
        } catch (e: Exception) {
            logger.warn("cannot copy file from {} to {}", inputFile, outputFile, e)
            false
        }
    }
}