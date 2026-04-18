package com.jixing.launcher.data.repository

import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.jixing.launcher.model.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 媒体管理数据仓库
 */
class MediaRepository(private val context: Context) {

    private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
    val currentMedia: Flow<MediaInfo?> = _currentMedia.asStateFlow()

    private val _playlist = MutableStateFlow<List<MediaInfo>>(emptyList())
    val playlist: Flow<List<MediaInfo>> = _playlist.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: Flow<Boolean> = _isPlaying.asStateFlow()

    private val _audioFiles = MutableStateFlow<List<MediaInfo>>(emptyList())
    val audioFiles: Flow<List<MediaInfo>> = _audioFiles.asStateFlow()

    private val _videoFiles = MutableStateFlow<List<MediaInfo>>(emptyList())
    val videoFiles: Flow<List<MediaInfo>> = _videoFiles.asStateFlow()

    suspend fun loadAudioFiles() = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<MediaInfo>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(collection, id.toString())

                audioList.add(
                    MediaInfo(
                        id = id,
                        title = cursor.getString(titleColumn) ?: "Unknown",
                        artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                        album = cursor.getString(albumColumn) ?: "Unknown Album",
                        duration = cursor.getLong(durationColumn),
                        albumId = cursor.getLong(albumIdColumn),
                        uri = contentUri.toString()
                    )
                )
            }
        }

        _audioFiles.value = audioList
    }

    suspend fun loadVideoFiles() = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<MediaInfo>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(collection, id.toString())

                videoList.add(
                    MediaInfo(
                        id = id,
                        title = cursor.getString(titleColumn) ?: "Unknown",
                        artist = cursor.getString(artistColumn) ?: "Unknown",
                        duration = cursor.getLong(durationColumn),
                        uri = contentUri.toString()
                    )
                )
            }
        }

        _videoFiles.value = videoList
    }

    fun setCurrentMedia(media: MediaInfo?) {
        _currentMedia.value = media
    }

    fun setPlaylist(list: List<MediaInfo>) {
        _playlist.value = list
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun playNext() {
        val currentIndex = _playlist.value.indexOf(_currentMedia.value)
        val nextIndex = if (currentIndex < _playlist.value.size - 1) currentIndex + 1 else 0
        _currentMedia.value = _playlist.value.getOrNull(nextIndex)
    }

    fun playPrevious() {
        val currentIndex = _playlist.value.indexOf(_currentMedia.value)
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else _playlist.value.size - 1
        _currentMedia.value = _playlist.value.getOrNull(prevIndex)
    }

    fun getAlbumArtUri(albumId: Long): Uri? {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }
}
