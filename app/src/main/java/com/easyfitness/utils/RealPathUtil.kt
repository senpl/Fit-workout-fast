package com.easyfitness.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video

object RealPathUtil {
    fun getRealPath(context: Context, fileUri: Uri): String? {
        val realPath: String?
        // SDK < API11
        realPath = getRealPathFromURI_API19(context, fileUri)
        return realPath
    }


    @SuppressLint("NewApi")
    fun getRealPathFromURI_API11to18(context: Context?, contentUri: Uri?): String? {
        val proj = arrayOf<String?>(Images.Media.DATA)
        var result: String? = null

        val cursorLoader = CursorLoader(context, contentUri, proj, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
        }
        return result
    }

    fun getRealPathFromURI_BelowAPI11(context: Context, contentUri: Uri): String? {
        val proj = arrayOf<String?>(Images.Media.DATA)
        val cursor = context.getContentResolver().query(contentUri, proj, null, null, null)
        var column_index = 0
        var result: String? = ""
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
            return result
        }
        return result
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    fun getRealPathFromURI_API19(context: Context, uri: Uri): String? {
        // DocumentProvider

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split: Array<String?> =
                    docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split: Array<String?> =
                    docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf<String?>(
                    split[1]
                )

                return RealPathUtil.getDataColumn(context, contentUri!!, selection, selectionArgs)
            }
        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {
            // Return the remote address

            if (isGooglePhotosUri(uri)) return uri.getLastPathSegment()

            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
            return uri.getPath()
        }

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri, selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        val column = "_data"
        val projection = arrayOf<String?>(
            column
        )
        context.getContentResolver().query(
            uri, projection, selection, selectionArgs,
            null
        ).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.getAuthority()
    }
}
