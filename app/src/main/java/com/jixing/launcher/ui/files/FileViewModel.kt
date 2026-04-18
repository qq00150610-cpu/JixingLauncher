package com.jixing.launcher.ui.files

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jixing.launcher.model.FileInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * 文件管理器 ViewModel
 */
@HiltViewModel
class FileViewModel @Inject constructor() : ViewModel() {

    private val _currentPath = MutableStateFlow(Environment.getExternalStorageDirectory().absolutePath)
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    private val _files = MutableStateFlow<List<FileInfo>>(emptyList())
    val files: StateFlow<List<FileInfo>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val pathHistory = mutableListOf<String>()

    init {
        loadFiles()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    val directory = File(_currentPath.value)
                    val fileList = directory.listFiles()?.map { file ->
                        FileInfo(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = if (file.isFile) file.length() else 0,
                            lastModified = file.lastModified(),
                            mimeType = getMimeType(file)
                        )
                    } ?: emptyList()

                    _files.value = fileList.sortedWith(
                        compareBy({ !it.isDirectory }, { it.name.lowercase() })
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openFile(file: FileInfo) {
        if (file.isDirectory) {
            pathHistory.add(_currentPath.value)
            _currentPath.value = file.path
            loadFiles()
        }
    }

    fun goBack() {
        if (pathHistory.isNotEmpty()) {
            _currentPath.value = pathHistory.removeAt(pathHistory.lastIndex)
            loadFiles()
        }
    }

    fun canGoBack(): Boolean = pathHistory.isNotEmpty()

    fun refresh() {
        loadFiles()
    }

    fun navigateTo(path: String) {
        if (File(path).exists() && File(path).isDirectory) {
            pathHistory.add(_currentPath.value)
            _currentPath.value = path
            loadFiles()
        }
    }

    private fun getMimeType(file: File): String {
        return when {
            file.isDirectory -> "folder"
            else -> when (file.extension.lowercase()) {
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "image/${file.extension}"
                "mp4", "avi", "mkv", "mov", "wmv" -> "video/${file.extension}"
                "mp3", "wav", "flac", "aac", "ogg" -> "audio/${file.extension}"
                "pdf" -> "application/pdf"
                "txt", "log", "xml", "json" -> "text/plain"
                "doc", "docx" -> "application/msword"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                "zip", "rar", "7z", "tar", "gz" -> "application/zip"
                else -> "application/octet-stream"
            }
        }
    }
}
