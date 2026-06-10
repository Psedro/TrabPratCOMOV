package com.example.estagios.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toTextRequestBody(): RequestBody {
    return this.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun Context.getFileName(uri: Uri): String {
    var result = "curriculo"

    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        if (cursor.moveToFirst() && nameIndex >= 0) {
            result = cursor.getString(nameIndex)
        }
    }

    return result
}

fun Context.uriToMultipart(
    uri: Uri,
    fieldName: String = "cv"
): MultipartBody.Part {
    val fileName = getFileName(uri)
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

    val bytes = contentResolver.openInputStream(uri)?.use {
        it.readBytes()
    } ?: throw IllegalArgumentException("Não foi possível ler o ficheiro.")

    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

    return MultipartBody.Part.createFormData(
        fieldName,
        fileName,
        requestBody
    )
}