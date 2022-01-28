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

    private val mutableButtonName = MutableLiveData(Constants.PLAY)
    val buttonName: LiveData<String> get() = mutableButtonName

    private val mutableCurrentMediaFrameText = MutableLiveData(0.toString())
    val currentMediaFrameText: LiveData<String> get() = mutableCurrentMediaFrameText

    private val mutableCurrentProjectFrameText = MutableLiveData(0.toString())
    val currentProjectFrameText: LiveData<String> get() = mutableCurrentProjectFrameText

    private val mutableSliderMaxValue = MutableLiveData(Constants.ITEM_SIZE - 1)
    val sliderMaxValue: LiveData<Int> get() = mutableSliderMaxValue

    private val mutableSliderEnabled = MutableLiveData(true)
    val sliderEnabled: LiveData<Boolean> get() = mutableSliderEnabled

    private val mutableSpeedCurveEnabled = MutableLiveData(false)
    val speedCurveEnabled: LiveData<Boolean> get() = mutableSpeedCurveEnabled

    private val mutableMediaFrameValue = MutableLiveData(0)
    val mediaFrameValue: LiveData<Int> get() = mutableMediaFrameValue

    private val mutableProjectFrameValue = MutableLiveData(0)
    val projectFrameValue: LiveData<Int> get() = mutableProjectFrameValue

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

    private val mutableProjectAndMediaFrames = MutableLiveData(hashMapOf<Int, Int>())

    private val mutableIsSpeedCurveValuesInRange = MutableLiveData(false)

    /** This function collects the media and project frames from repository **/
    suspend fun collectImages(
        currentProjectFrameValue: Int,
    ) {
        repository.collectFrames(currentProjectFrameValue, mutableProjectAndMediaFrames.value!!)
            .flowOn(Dispatchers.Default).collect {
                if (currentProjectFrameValue == mutableProjectAndMediaFrames.value?.size?.minus(1)) {
                    // When it comes to the last value, it starts from 0th project frame.
                    mutableProjectFrameValue.postValue(0)
                } else {
                    // It is updated continuously, so if it is paused, it can start from the paused index.
                    mutableProjectFrameValue.postValue(it.index)
                }
                mutableMediaFrameValue.postValue(it.value) // current media frame is continuously updated.
            }
    }

    /** This function is triggered when play/pause button is clicked.
     * If the button is clicked when it is played, it is paused so mutableIsPlaying is set to false
     * and button text is set to "Play" (and vice versa)
     * @param context **/
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

    /** This function updates the current media frame according to slider position.
     * Also, current media frame text and current project frame text are updated accordingly.
     * @param position position of the slider **/
    fun updateCurrentMediaFrameWithSliderPosition(position: Int) {
        mutableMediaFrameValue.postValue(
            mutableProjectAndMediaFrames.value?.getValue(position)
        )
        mutableProjectFrameValue.postValue(
            mutableProjectAndMediaFrames.value?.keys?.elementAt(position)
        )
        mutableCurrentMediaFrameText.postValue(
            mutableProjectAndMediaFrames.value?.getValue(position).toString()
        )
        mutableCurrentProjectFrameText.postValue(
            mutableProjectAndMediaFrames.value?.keys?.elementAt(position).toString()
        )

    }

    /** This function sets isPlaying live variable to true.
     * Also, current media frame text and current project frame text are restarted from 0. **/
    fun startPlaying() {
        // This if condition prevents to start playing without button click in the app launch
        if (!mutableIsFirstLaunch.value!! && mutableIsSpeedCurveValuesInRange.value!!) {
            mutableIsPlaying.postValue(true)
            mutableCurrentProjectFrameText.postValue(0.toString())
            mutableCurrentMediaFrameText.postValue(0.toString())
        }
    }

    /** This function validates if the values of EdiTexts in SpeedCurveFragment
     * are set correctly by the user.
     * @param speed1 speed of pointer 1
     * @param speed2 speed of pointer 2
     * @param speed3 speed of pointer 3
     * @param index2 index of pointer 2 **/
    fun validateEditTextValue(
        speed1: String,
        speed2: String,
        speed3: String,
        index2: String,
        work: () -> Unit
    ): Boolean {
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

    fun enableSpeedCurveFragment(isEnabled: Boolean) {
        mutableSpeedCurveEnabled.postValue(isEnabled)
    }

    /** This function creates and returns the hash map of project and media frames. Each media frame is obtained
     * by the times of repeating print number of elements. Project frames are the index of the hash map.
     * @param index2 index of pointer 2
     * @param speed1 speed of pointer 1
     * @param speed2 speed of pointer 2
     * @param speed3 speed of pointer 3
     * @return the project and media frames hash map created **/
    fun generateProjectAndMediaFrames(
        index2: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float
    ): HashMap<Int, Int> {
        val hashMap: HashMap<Int, Int> = HashMap()
        var i = 0
        for ((index, value) in repository.createListOfPrintNumbersOfFrames(
            index2,
            speed1,
            speed2,
            speed3
        ).withIndex()) {
            repeat(value) {
                hashMap[i++] = index
            }
        }
        mutableProjectAndMediaFrames.postValue(hashMap)
        return hashMap
    }

    fun setSpeedAndIndexValues(speed1: Float, speed2: Float, speed3: Float, index: Int) {
        mutableSpeed1.postValue(speed1)
        mutableSpeed2.postValue(speed2)
        mutableSpeed3.postValue(speed3)
        mutableIndex.postValue(index)
    }

    fun setCurrentProjectFrameValue(startPosition: Int) {
        mutableProjectFrameValue.postValue(startPosition)
    }

    fun setSliderMaxValue(sliderMaxValue: Int) {
        mutableSliderMaxValue.postValue(sliderMaxValue)
    }

    fun setIsNotFirstLaunch() {
        mutableIsFirstLaunch.postValue(false)
    }

    fun setIsSpeedCurveValuesInRange(isSpeedCurveValuesInRange: Boolean) {
        mutableIsSpeedCurveValuesInRange.postValue(isSpeedCurveValuesInRange)
    }
}