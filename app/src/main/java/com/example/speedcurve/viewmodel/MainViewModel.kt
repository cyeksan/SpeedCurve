package com.example.speedcurve.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.speedcurve.R
import com.example.speedcurve.repository.FrameInfoGenerator
import com.example.speedcurve.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FrameInfoGenerator
) : ViewModel() {

    private val mutableIsFirstLaunch = MutableLiveData(true)

    private val mutableButtonName = MutableLiveData<String>()
    val buttonName: LiveData<String> get() = mutableButtonName

    private val mutableCurrentMediaFrameText = MutableLiveData(0.toString())
    val currentMediaFrameText: LiveData<String> get() = mutableCurrentMediaFrameText

    private val mutableCurrentProjectFrameText = MutableLiveData(0.toString())
    val currentProjectFrameText: LiveData<String> get() = mutableCurrentProjectFrameText

    private val mutableSliderPosition = MutableLiveData(0)
    val sliderPosition: LiveData<Int> get() = mutableSliderPosition

    private val mutableSliderMaxValue = MutableLiveData(Constants.ITEM_SIZE - 1)
    val sliderMaxValue: LiveData<Int> get() = mutableSliderMaxValue

    private val mutableSliderEnabled = MutableLiveData(true)
    val sliderEnabled: LiveData<Boolean> get() = mutableSliderEnabled

    private val mutableSpeedCurveEnabled = MutableLiveData(false)
    val speedCurveEnabled: LiveData<Boolean> get() = mutableSpeedCurveEnabled

    private val mutableFrameValue = MutableLiveData(0)
    val frameValue: LiveData<Int> get() = mutableFrameValue

    private val mutableIsPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> get() = mutableIsPlaying

    private val mutableSpeed1 = MutableLiveData(Constants.INITIAL_SPEED)
    val speed1: LiveData<Float> get() = mutableSpeed1

    private val mutableSpeed2 = MutableLiveData(Constants.INITIAL_SPEED)
    val speed2: LiveData<Float> get() = mutableSpeed2

    private val mutableSpeed3 = MutableLiveData(Constants.INITIAL_SPEED)
    val speed3: LiveData<Float> get() = mutableSpeed3

    private val mutableIndex = MutableLiveData(Constants.INITIAL_INDEX)
    val index: LiveData<Int> get() = mutableIndex

    private val mutableStartPosition = MutableLiveData(0)
    val startPosition: LiveData<Int> get() = mutableStartPosition

    private val mutableIsSpeedCurveValuesInRange = MutableLiveData(false)

    suspend fun collectImages(
        speed: Float,
        startPosition: Int,
        speed2: Float,
        speed3: Float,
        index: Int
    ) {
        repository.generateFrames(startPosition, speed, speed2, speed3, index)
            .flowOn(Dispatchers.Default).collect {
                if (startPosition == getProjectFrames(speed, index, speed2, speed3).size - 1) {
                    // When it comes to the last value, it starts from 0th project frame.
                    mutableStartPosition.postValue(0)
                } else {
                    // It is updated continuously, so if it is paused, it can start from the paused index.
                    mutableStartPosition.postValue(it.index)
                }
                mutableFrameValue.postValue(it.value)
            }
    }

    fun togglePlaying(context: Context) {
        if (isPlaying.value!!) {
            mutableIsPlaying.postValue(false)
            mutableButtonName.postValue(context.getString(R.string.play_btn_text))
        } else {
            mutableIsPlaying.postValue(true)
            mutableButtonName.postValue(context.getString(R.string.pause_btn_text))
        }
        mutableSliderEnabled.value = isPlaying.value!!
    }

    fun changeSliderPosition(position: Int) {
        mutableSliderPosition.postValue(position)
        mutableCurrentProjectFrameText.postValue(position.toString())
        try {
            mutableFrameValue.postValue(
                getProjectFrames(
                    speed1.value!!, index.value!!,
                    speed2.value!!, speed3.value!!
                )[position]
            )
            mutableCurrentMediaFrameText.postValue(
                getProjectFrames(
                    speed1.value!!, index.value!!,
                    speed2.value!!, speed3.value!!
                )[position].toString()
            )

        } catch (ex: IndexOutOfBoundsException) {
            ex.printStackTrace()
        }
    }

    fun enableSpeedCurveFragment(isEnabled: Boolean) {
        mutableSpeedCurveEnabled.postValue(isEnabled)
    }

    fun setSpeedAndIndexValues(speed1: Float, speed2: Float, speed3: Float, index: Int) {
        mutableSpeed1.postValue(speed1)
        mutableSpeed2.postValue(speed2)
        mutableSpeed3.postValue(speed3)
        if (index == 0) {
            mutableIndex.postValue(index + 1)
        } else {
            mutableIndex.postValue(index)
        }
    }

    fun setStartPositionValue(startPosition: Int) {
        mutableStartPosition.postValue(startPosition)
    }

    fun setSliderMaxValue(sliderMaxValue: Int) {
        mutableSliderMaxValue.postValue(sliderMaxValue)
    }

    fun setIsNotFirstLaunch() {
        mutableIsFirstLaunch.postValue(false)
    }

    fun startPlaying(context: Context) {
        if (!mutableIsFirstLaunch.value!! && mutableIsSpeedCurveValuesInRange.value!!) {
            mutableIsPlaying.postValue(true)
            mutableButtonName.postValue(context.getString(R.string.pause_btn_text))
            mutableSliderEnabled.postValue(false)
        }
        mutableCurrentProjectFrameText.postValue(0.toString())
        mutableCurrentMediaFrameText.postValue(0.toString())

    }

    fun setIsSpeedCurveValuesInRange(isSpeedCurveValuesInRange: Boolean) {
        mutableIsSpeedCurveValuesInRange.postValue(isSpeedCurveValuesInRange)
    }

    fun getProjectFrames(speed: Float, index: Int, speed2: Float, speed3: Float) =
        repository.createProjectFrames(index, speed, speed2, speed3)

    fun validateEditTextValue(
        speed1: String,
        speed2: String,
        speed3: String,
        index2: String,
        work: () -> Unit
    ) : Boolean{
        if (speed1.isNotEmpty() && speed2.isNotEmpty() && speed3.isNotEmpty() && index2.isNotEmpty()) {
            if (speed1.toFloat() in Constants.SPEED_LOWER_LIMIT..Constants.SPEED_HIGHER_LIMIT &&
                speed2.toFloat() in Constants.SPEED_LOWER_LIMIT..Constants.SPEED_HIGHER_LIMIT &&
                speed3.toFloat() in Constants.SPEED_LOWER_LIMIT..Constants.SPEED_HIGHER_LIMIT &&
                index2.toInt() in 0 until Constants.ITEM_SIZE
            ) {
                work()
                return true
            }
        }

        return false
    }
}