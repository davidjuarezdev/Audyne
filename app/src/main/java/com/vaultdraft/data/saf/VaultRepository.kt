package com.vaultdraft.data.saf

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.vaultdraft.domain.capture.CaptureEngine
import com.vaultdraft.domain.model.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VaultRepository(private val context: Context) {

    fun takePersistablePermission(uri: Uri) {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        }
    }

    fun hasAccess(vaultUri: Uri?): Boolean {
        if (vaultUri == null) return false
        return runCatching {
            val tree = DocumentFile.fromTreeUri(context, vaultUri) ?: return false
            tree.canRead() && tree.canWrite()
        }.getOrElse { false }
    }

    suspend fun writeCapture(vaultUri: Uri, route: Route, content: String): CaptureEngine.Result =
        withContext(Dispatchers.IO) {
            val tree = DocumentFile.fromTreeUri(context, vaultUri)
                ?: throw SecurityException("Vault folder is not accessible")
            val result = CaptureEngine.buildCapture(route, content, readExisting(tree, route))
            val file = resolveFile(tree, result.targetPath)
            write(file, result.newContent)
            result
        }

    private fun readExisting(tree: DocumentFile, route: Route): String? {
        val path = com.vaultdraft.domain.tokens.TokenEngine.expand(route.pathTemplate)
        return runCatching {
            val file = findFile(tree, path) ?: return null
            context.contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }

    private fun resolveFile(tree: DocumentFile, path: String): DocumentFile {
        val existing = findFile(tree, path)
        if (existing != null) return existing
        val segments = path.trim('/').split('/')
        var current = tree
        segments.forEachIndexed { index, segment ->
            val isFile = index == segments.lastIndex
            current = if (isFile) {
                current.createFile("application/octet-stream", segment)
                    ?: throw java.io.IOException("Could not create $path")
            } else {
                current.findFile(segment) ?: current.createDirectory(segment)
                    ?: throw java.io.IOException("Could not create directory $segment")
            }
        }
        return current
    }

    private fun findFile(tree: DocumentFile, path: String): DocumentFile? {
        val segments = path.trim('/').split('/')
        var current: DocumentFile? = tree
        for (segment in segments) {
            current = current?.findFile(segment) ?: return null
        }
        return current?.takeIf { it.isFile }
    }

    private fun write(file: DocumentFile, content: String) {
        context.contentResolver.openOutputStream(file.uri, "w")?.use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        } ?: throw java.io.IOException("Could not open ${file.uri} for writing")
    }
}
