package com.gordon.hkrainfallmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

typealias RainfallRange = Pair<Double, Double>

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

    private val _lastRainfallDataUpdateTimestamp : MutableLiveData<Date?> = MutableLiveData(null)
    val lastRainfallDataUpdateTimestamp : LiveData<Date?> = _lastRainfallDataUpdateTimestamp

    val showTimeMenu : MutableLiveData<Boolean> = MutableLiveData(false)


    private val _location : MutableLiveData<LatLng?> = MutableLiveData(null)
    val location : LiveData<LatLng?> = _location

    private val _lastLocationUpdateTimestamp : MutableLiveData<Date?> = MutableLiveData(null)
    val lastLocationUpdateTimestamp : LiveData<Date?> = _lastLocationUpdateTimestamp

    private val _currentLocationRainFallRange : MutableLiveData<RainfallRange?> = MutableLiveData(null)
    val currentLocationRainFallRange : LiveData<RainfallRange?> = _currentLocationRainFallRange


    fun updateRainfallDataSet () {
        _isFetchingData.postValue(true)
        apiManager.getRainfallDataSet( RainfallAPICallback(onDataReceived = {

            GlobalScope.launch {
                withContext(Dispatchers.Main) {

                    _lastRainfallDataUpdateTimestamp.value = Date()
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
                    _lastRainfallDataUpdateTimestamp.value = Date()

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


        tryFindCurrentLocationRainfallRange()
    }

    fun updateCurrentLocation(location : LatLng){
        _location.postValue(location)
        _lastLocationUpdateTimestamp.postValue(Date())

        tryFindCurrentLocationRainfallRange()

    }

    private fun tryFindCurrentLocationRainfallRange()  {

        val location = _location.value ?: return
        val dataSet = _rainfallDataSet.value ?: return
        val result = tryFindRainfallRange(location, dataSet)

        _currentLocationRainFallRange.postValue(result)

    }

    private fun tryFindRainfallRange(location: LatLng, rainfallData: RainfallDataSet) : RainfallRange? {

        val firstDataSet = rainfallData.values.first().values

        var dataLocation : LatLng? = null

        for( data in firstDataSet) {
            if (data.contain(location)) {
                dataLocation = data.position
                break
            }
        }

        val targetDataLocation = dataLocation ?: return  null
        val allDataSet = _rainfallDataSet.value?.values ?: return null

        var targetLocationData : MutableList<RainfallData> = mutableListOf()

        for (dataSet in allDataSet) {

            val data = dataSet[targetDataLocation] ?: continue
            targetLocationData.add(data)
        }

        if (targetLocationData.count() < 2) { return  null }

        targetLocationData.sortWith { a, b ->
            when {
                a.expectedRainfall > b.expectedRainfall -> 1
                a.expectedRainfall < b.expectedRainfall -> -1
                else -> 0
            }
        }

        return Pair(targetLocationData.first().expectedRainfall, targetLocationData.last().expectedRainfall)

    }

}
