@file:JvmName("PagingRequestHelper")
package com.tps.orientnews.utils

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagingRequestHelper
import com.tps.orientnews.data.NetworkState

private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
    try{
        return PagingRequestHelper.RequestType.values().mapNotNull {
            report.getErrorFor(it)?.message
        }.first()
    }
    catch (e:IllegalArgumentException){
        return e.localizedMessage;
    }

}

fun PagingRequestHelper.createStatusLiveData(): LiveData<NetworkState> {
    val liveData = MutableLiveData<NetworkState>()
    addListener { report ->
        when {
            report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
            report.hasError() -> liveData.postValue(
                    NetworkState.error(getErrorMessage(report)))
            else -> liveData.postValue(NetworkState.LOADED)
        }
    }
    return liveData
}
