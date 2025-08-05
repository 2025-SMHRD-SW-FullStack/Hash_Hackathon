package com.example.talent_link.data.model.signup

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

fun createSignupRequestBody(email: String, password: String, nickname: String): RequestBody {
    val json = JSONObject().apply {
        put("email", email)
        put("password", password)
        put("nickname", nickname)
    }
    return json.toString().toRequestBody("application/json".toMediaTypeOrNull())
}

fun prepareFilePart(uri: Uri, context: Context): MultipartBody.Part? {
    val contentResolver = context.contentResolver
    val fileType = contentResolver.getType(uri) ?: return null
    val inputStream = contentResolver.openInputStream(uri) ?: return null
    val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
    tempFile.outputStream().use { inputStream.copyTo(it) }

    val requestFile = tempFile.asRequestBody(fileType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("profileImage", tempFile.name, requestFile)
}