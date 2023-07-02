package com.gordon.hkrainfallmap

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class MapViewModel(apiManager: APIManagerType = APIManager()) : ViewModel() {

    private val apiManager: APIManagerType = apiManager

    private val _rainfallDataSet : MutableLiveData<RainfallDataSet> = MutableLiveData(mutableMapOf())
    val rainfallDataSet : LiveData<RainfallDataSet> = _rainfallDataSet

    private val _displayedRainfallDataMap : MutableLiveData<LatLngRainfallDataMap> = MutableLiveData(mutableMapOf())
    val displayedRainfallDataMap : LiveData<LatLngRainfallDataMap> = _displayedRainfallDataMap

    var sortedDatetimeString : List<DateTimeString> = listOf()

    private val _selectedDateTimeString : MutableLiveData<String> = MutableLiveData("")
    var selectedDateTimeString : LiveData<String> = _selectedDateTimeString

    private val _isFetchingData : MutableLiveData<Boolean> = MutableLiveData(false)
    val isFetchingData : LiveData<Boolean> = _isFetchingData

    val isLocationAccessGranted : MutableLiveData<Boolean> = MutableLiveData(false)

    val showMapLegend : MutableLiveData<Boolean> = MutableLiveData(false)

    private val _lastUpdateTimestamp : MutableLiveData<Date?> = MutableLiveData(null)
    val lastUpdateTimestamp : LiveData<Date?> = _lastUpdateTimestamp

    val showTimeMenu : MutableLiveData<Boolean> = MutableLiveData(false)

    fun updateRainfallDataSet () {
        _isFetchingData.postValue(true)
        apiManager.getRainfallDataSet( RainfallAPICallback(onDataReceived = {

            GlobalScope.launch {
                withContext(Dispatchers.Main) {

                    _lastUpdateTimestamp.value = Date()
                    _rainfallDataSet.value = it

                    sortedDatetimeString = it.keys.sortedWith(Comparator<DateTimeString>{ a, b ->
                        when {
                            a.toLongOrNull() ?: 0 >  b.toLongOrNull() ?: 0 -> 1
                            a.toLongOrNull() ?: 0 <  b.toLongOrNull() ?: 0 -> -1
                            else -> 0
                        }
                    })

                    updateDisplayedDataMap()

                    _isFetchingData.postValue(false)
                }
            }

        }) {
            // failure
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    _lastUpdateTimestamp.value = Date()

                    _rainfallDataSet.value = mutableMapOf()

                    _isFetchingData.postValue(false)
                }
            }
        })

    }

    fun updateDisplayedDataMap(datetimeString : String? = null){
        if(datetimeString.isNullOrEmpty() && sortedDatetimeString.isNotEmpty()) {
            _selectedDateTimeString.value = sortedDatetimeString.first()
        } else {
            _selectedDateTimeString.value = datetimeString
        }

        _displayedRainfallDataMap.value = rainfallDataSet.value?.get(_selectedDateTimeString.value) ?: mutableMapOf()
    }

}
