package com.anand.iconfinder.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.anand.iconfinder.data.Repository
import com.anand.iconfinder.model.Icon

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var recentQuery = "\"\""
    private var recentCount = 0
    private var recentIndex = 0
    private var latestData = MutableLiveData<List<Icon>>()

    fun getIcons(query: String, count: Int, index: Int): LiveData<List<Icon>> {
        if (query == recentQuery && recentCount == count && recentIndex == index) {
            return latestData
        }
        recentQuery = query
        recentCount = count
        recentIndex = index

        latestData = Repository.getIcons(query, count, index)

        return latestData
        // Above statements could be simplified further but for the sake
        // of readability and understanding, above approach was followed
    }

}