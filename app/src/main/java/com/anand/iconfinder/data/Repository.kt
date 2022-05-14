package com.anand.iconfinder.data

import androidx.lifecycle.MutableLiveData
import com.anand.iconfinder.BuildConfig
import com.anand.iconfinder.model.ApiResponse
import com.anand.iconfinder.model.Icon
import com.anand.iconfinder.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

object Repository {

    private val coroutineScope = Dispatchers.IO
    private val iconsLiveData = MutableLiveData<List<Icon>>()

    fun getIcons(query: String, count: Int, index: Int): MutableLiveData<List<Icon>> {
        isLoading = true
        val job = CoroutineScope(coroutineScope).launch {
            val request: Response<ApiResponse> = retrofitClient.getIcons(params(query, count, index))

            if (request.isSuccessful) {
                isLoading = false
                iconsLiveData.postValue(request.body()?.icons)
            } else {
                isLoading = false
                //iconsLiveData.postValue(listOf())
            }
        }

        return iconsLiveData
    }


    private fun params(query: String, count: Int, index: Int): Map<String, String> =
        mapOf(
            QUERY to query,
            COUNT to count.toString(),
            START_INDEX to index.toString(),
            CLIENT_ID to BuildConfig.CLIENT_ID,
            CLIENT_SECRET to BuildConfig.CLIENT_SECRET
        )
}