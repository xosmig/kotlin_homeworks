package xosmig.utils

import org.apache.commons.io.FilenameUtils
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Loads all classes in the hierarchy.
 * Returns the [List] of the loaded classes.
 *
 * @param [root] the path to the directory which is the root of the hierarchy of classes to be loaded
 * @return
 */
fun loadHierarchy(root: Path): List<Class<*>> {
    val cl = URLClassLoader(arrayOf(root.toUri().toURL()))
    val loadedClasses = ArrayList<Class<*>>()
    for (file in Files.walk(root).filter(::isClassFile)) {
        val builder = StringBuilder()
        for (token in root.relativize(file)) {
            builder.append(token)
            builder.append(".")
        }
        if (builder.last() == '.') {
            builder.deleteCharAt(builder.length - 1)
        }
        loadedClasses.add(cl.loadClass(builder.toString()))
    }
    return loadedClasses
}

private fun isClassFile(path: Path): Boolean {
    return Files.isRegularFile(path) && FilenameUtils.getExtension(path.fileName.toString()) == ".class"
}
