package utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.*
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipSupporter {
    val logger: Logger = LogManager.getLogger()

    fun zip(input: Path, output: Path, eraseInput: Boolean) {
        if (!Files.exists(input)) {
            return
        }
        val success = if (Files.isDirectory(input)) zipDir(input, output) else zipFile(input, output)

        if (success && eraseInput) {
            FileSupporter.deleteIfExists(input)
        }
    }

    private fun zipFile(input: Path, output: Path): Boolean {
        if (Files.exists(output)) {
            return false
        }
        try {
            FileOutputStream(output.toFile()).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    zos.putNextEntry(ZipEntry(input.fileName.toString()))
                    Files.copy(input, zos)
                    zos.closeEntry()
                    return true
                }
            }
        } catch (e: IOException) {
            logger.warn("cannot zip {} to {} by ", input, output, e)
            FileSupporter.deleteIfExists(output)
            return false
        }
    }

    private fun zipDir(input: Path, output: Path): Boolean {
        if (Files.exists(output)) {
            return false
        }

        try {
            FileOutputStream(output.toFile()).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    Files.walkFileTree(input, object : SimpleFileVisitor<Path>() {
                        @Throws(IOException::class)
                        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                            if (input != dir) {
                                zos.putNextEntry(ZipEntry(input.relativize(dir).toString() + "/"))
                                zos.closeEntry()
                            }
                            return FileVisitResult.CONTINUE
                        }

                        @Throws(IOException::class)
                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            zos.putNextEntry(ZipEntry(input.relativize(file).toString()))
                            Files.copy(file, zos)
                            zos.closeEntry()
                            return FileVisitResult.CONTINUE
                        }
                    })
                    return true
                }
            }
        } catch (e: IOException) {
            logger.warn("cannot zip {} to {} by ", input, output, e)
            FileSupporter.deleteIfExists(output)
            return false
        }
    }

    fun zips(input: List<Path>, output: Path, eraseInput: Boolean) {
        if (Files.exists(output)) {
            return
        }
        if (!Files.exists(output.parent)) {
            try {
                Files.createDirectories(output.parent)
            } catch (e: Exception) {
                logger.warn("cannot create parent of {} by ", output, e)
                return
            }
        }

        try {
            FileOutputStream(output.toFile()).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    for (source in input) {
                        if (Files.isDirectory(source)) {
                            zipDir(source, zos)
                        } else {
                            zipFile(source, zos)
                        }
                    }
                    if (eraseInput) {
                        for (source in input) {
                            FileSupporter.deleteIfExists(source)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            logger.warn("cannot zip {} to {} by ", input, output, e)
            FileSupporter.deleteIfExists(output)
        }
    }

    @Throws(IOException::class)
    private fun zipFile(source: Path, zos: ZipOutputStream) {
        zos.putNextEntry(ZipEntry(source.fileName.toString()))
        Files.copy(source, zos)
        zos.closeEntry()
    }

    @Throws(IOException::class)
    private fun zipDir(source: Path, zos: ZipOutputStream) {
        zos.putNextEntry(ZipEntry(source.fileName.toString() + "/"))
        zos.closeEntry()
        Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (source != dir) {
                    zos.putNextEntry(ZipEntry(source.parent.relativize(dir).toString() + "/"))
                    zos.closeEntry()
                }
                return FileVisitResult.CONTINUE
            }

            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                zos.putNextEntry(ZipEntry(source.parent.relativize(file).toString()))
                Files.copy(file, zos)
                zos.closeEntry()
                return FileVisitResult.CONTINUE
            }
        })
    }

    fun unzip(input: Path, output: Path) {
        if (Files.exists(output) && !Files.isDirectory(output)) {
            logger.error("cannot unzip {} to file {} ", input, output)
            return
        }
        try {
            FileInputStream(input.toFile()).use { fis ->
                ZipInputStream(BufferedInputStream(fis)).use { zis ->
                    if (!Files.exists(output)) {
                        Files.createDirectories(output)
                    }

                    var entry: ZipEntry? = zis.nextEntry

                    while (entry != null) {
                        val fileName = entry.name

                        val file = output.resolve(fileName)

                        if (!entry.isDirectory) {
                            unzipFileContent(file, zis)
                        } else {
                            if (!Files.exists(file)) {
                                Files.createDirectories(file)
                            }
                        }

                        zis.closeEntry()

                        entry = zis.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("cannot unzip {} to {} by ", input, output, e)
        }
    }

    private fun unzipFileContent(file: Path, zis: ZipInputStream) {
        try {
            FileOutputStream(file.toFile()).use { fos ->
                BufferedOutputStream(fos, 2048).use { bos ->
                    var len: Int
                    val data = ByteArray(2048)
                    while (zis.read(data, 0, 2048).also { len = it } != -1) {
                        bos.write(data, 0, len)
                    }
                }
            }
        }
        catch (ignored: FileNotFoundException) { }
        catch (e: Exception) {
            logger.error("cannot unzip {}", file, e)
        }
    }
}