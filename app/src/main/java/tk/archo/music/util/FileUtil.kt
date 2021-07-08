package tk.archo.music.util

import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class FileUtil {
    companion object {
        fun readFile(path: String): String {
            val file = File(path)
            val stringBuilder = StringBuilder()
            val fileReader: FileReader
            try {
                fileReader = FileReader(file)
                val buffer = CharArray(1024)
                var length: Int
                while (fileReader.read(buffer).also { length = it } > 0) {
                    stringBuilder.append(String(buffer, 0, length))
                }
                fileReader.close()
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
            return stringBuilder.toString()
        }

        fun writeStringToFile(path: String, content: String) {
            val file = File(path)
            if (!doesFileExists(path)) {
                createFile(path)
            }
            var fileWriter: FileWriter? = null
            try {
                fileWriter = FileWriter(file, false)
                fileWriter.write(content)
                fileWriter.flush()
                fileWriter.close()
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }

        fun createFile(path: String) {
            val newFile = File(path)
            try {
                if (!newFile.parentFile.exists()) {
                    createDirectory(newFile.parentFile.toString())
                }
                if (!newFile.exists()) {
                    newFile.createNewFile()
                }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }

        fun createDirectory(path: String) {
            val newDirectory = File(path)
            if (!newDirectory.exists()) {
                try {
                    newDirectory.mkdirs()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

        fun renameFile(originPath: String, newPath: String) {
            val originFile = File(originPath)
            val renamedFile = File(newPath)
            try {
                originFile.renameTo(renamedFile)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        fun deleteFile(path: String) {
            val file = File(path)
            if (file.isFile) {
                file.delete()
            } else {
                throw RuntimeException("Unable to delete file $path")
            }
        }

        fun getFileExtension(path: String?): String? {
            return MimeTypeMap.getFileExtensionFromUrl(path)
        }

        fun getFileMimeType(path: String?): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(path)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }

        @Deprecated("API 29", ReplaceWith(""))
        fun getExternalStorageDir(): String? {
            return Environment.getExternalStorageDirectory().absolutePath
        }

        fun getPackageDir(context: Context): String {
            return context.getExternalFilesDir(null)!!.absolutePath
        }

        fun doesFileExists(path: String): Boolean {
            val file = File(path)
            return file.exists()
        }

        fun isFile(path: String): Boolean {
            val file = File(path)
            return file.isFile
        }

        fun isDirectory(path: String): Boolean {
            val directory = File(path)
            return directory.isDirectory
        }
    }
}