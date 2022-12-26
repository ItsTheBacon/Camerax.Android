package com.bacon.data.base

import android.util.Log
import android.webkit.MimeTypeMap
import com.bacon.data.BuildConfig
import com.bacon.data.utils.DataMapper
import com.bacon.domain.utils.Either
import com.bacon.domain.utils.NetworkError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

abstract class BaseRepository {

    /**
     * Do network request
     *
     * @return result in [flow] with [Either]
     */
    protected fun <T : DataMapper<S>, S> doNetworkRequest(
        request: suspend () -> Response<T>
    ) = flow {
        request().let {
            when {
                it.isSuccessful && it.body() != null -> {
                    emit(Either.Right(it.body()!!.mapToDomain()))
                }
                !it.isSuccessful && it.code() == 422 -> {
                    emit(Either.Left(NetworkError.ApiInputs(it.errorBody().toApiError())))
                }
                else -> {
                    emit(Either.Left(NetworkError.Api(it.errorBody().toApiError())))
                }
            }
        }
    }.flowOn(Dispatchers.IO).catch { exception ->
        val message = exception.localizedMessage ?: "Error Occurred!"
        if (BuildConfig.DEBUG) {
            Log.d(this@BaseRepository.javaClass.simpleName, message)
        }
        emit(Either.Left(NetworkError.Unexpected(message)))
    }

    /**
     * Convert network error from server side
     */
    private inline fun <reified T> ResponseBody?.toApiError(): T {
        return Gson().fromJson(
            this?.string(), object : TypeToken<T>() {}.type
        )
    }

    /**
     * Get non-nullable body from request
     */
    protected inline fun <T : Response<S>, S> T.onSuccess(block: (S) -> Unit): T {
        this.body()?.let(block)
        return this
    }

    /**
     * Convert [File] to [MultipartBody.Part]
     */
    fun File.toMultipartBodyPart(formDateName: String) = MultipartBody.Part.createFormData(
        name = formDateName,
        filename = name,
        body = asRequestBody(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.toMediaTypeOrNull()
        )
    )

}