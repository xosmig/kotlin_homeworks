package com.xosmig.utils

import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FilenameUtils.getExtension
import org.apache.commons.io.FilenameUtils.removeExtension
import java.io.IOException
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path

/**
 * Loads all classes in the hierarchy.
 * Returns the [List] of the loaded classes.
 *
 * @param [root] the path to the directory which is the root of the hierarchy of classes to be loaded
 * @return the [List] of the loaded classes.
 */
@Throws(IOException::class)
fun loadHierarchy(root: Path): List<Class<*>> {
    val cl = URLClassLoader(arrayOf(root.toUri().toURL()))
    val loadedClasses = ArrayList<Class<*>>()
    for (file in Files.walk(root).filter(::isClassFile)) {
        val builder = StringBuilder()
        for (token in root.relativize(file.parent)) {
            builder.append(token)
            builder.append(".")
        }
        builder.append(removeExtension(file.fileName.toString()))
        loadedClasses.add(cl.loadClass(builder.toString()))
    }
    return loadedClasses
}

@Throws(IOException::class)
private fun isClassFile(path: Path): Boolean {
    return Files.isRegularFile(path) && getExtension(path.fileName.toString()) == "class"
}
