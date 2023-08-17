package com.gordon.hkrainfallmap

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.net.URL

class SettingsViewModel(context: Context) : ViewModel() {

    val context = context
    val showAppInfo : MutableLiveData<Boolean> = MutableLiveData(false)

    private val appVersion = "1.2"

    fun getAppInfoArray() : List<Pair<String, URL?>> {

        return listOf(
            Pair(context.getString(R.string.about_this_app_data_source), URL("https://data.gov.hk/en-datasets/category/climate-and-weather")),
            Pair(context.getString(R.string.about_this_app_developer_github), URL("https://github.com/GordonOnGithub")),
            Pair(context.getString(R.string.about_this_app_app_version) + appVersion, null),
            )
    }

    fun onRateThisAppClicked(){
        openURLExternally(URL("https://play.google.com/store/apps/details?id=com.gordon.hkrainfallmap"))
    }

    fun onAboutThisAppClicked(){

        showAppInfo.postValue(true)

    }

    fun openURLExternally(url : URL?){

        url?.let {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.toString()))

            context.startActivity(browserIntent)
        }


    }
}