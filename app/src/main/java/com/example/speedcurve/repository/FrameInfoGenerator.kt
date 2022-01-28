package com.example.speedcurve.repository

import com.example.speedcurve.model.FrameInfo
import com.example.speedcurve.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FrameInfoGenerator @Inject constructor() {
    private val frameInfo = FrameInfo()
    private val speedArray = FloatArray(Constants.ITEM_SIZE)

    private fun createSpeedArrayOfFrames(
        index2: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float
    ): FloatArray {
        val index1 = 0
        val index3 = Constants.ITEM_SIZE - 1
        for (i in 0..index3) {
            if (i <= index2) {
                speedArray[i] = linearInterpolation(speed1, i, speed2, speed1, index2, index1)
            } else {
                speedArray[i] =
                    linearInterpolation(speed2, i, speed3, speed2, index3, index2)
            }
        }
        return speedArray
    }

    fun createListOfPrintNumbersOfFrames(
        index2: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float
    ): List<Int> {
        val printNumberList = arrayListOf<Int>()

        var remainder = 0.0f

        for (speedValue in createSpeedArrayOfFrames(index2, speed1, speed2, speed3).toList()) {
            remainder += (1 / speedValue) % 1 // 1 / speedValue is frame display time
            var division: Int = ((1 / speedValue) / 1).toInt()

            if ((division == 1 && remainder > 0.0f) || remainder >= 1.0f) {
                remainder %= 1.0f
                division++
            }

            printNumberList.add(division)
        }
        // Rotate the list until the first index of print number list is different from 0.
        // It means that 0th project frame must start from the 0th media frame.
        while (printNumberList[0] == 0) {
            Collections.rotate(printNumberList, -1)
        }

        return printNumberList
    }

    fun collectFrames(
        currentProjectFrameValue: Int,
        projectAndMediaFrames: HashMap<Int, Int>
    ): Flow<FrameInfo> = flow {

        for (i in currentProjectFrameValue until projectAndMediaFrames.size) {
            frameInfo.index = projectAndMediaFrames.keys.elementAt(i)
            frameInfo.value = projectAndMediaFrames.getValue(i)
            emit(frameInfo)
            delay(Constants.DELAY_TIME)
        }
    }

    private fun linearInterpolation(
        initialSpeed: Float, currentIndex: Int,
        y1: Float, y0: Float, x1: Int, x0: Int
    ): Float {
        return initialSpeed + (currentIndex + 1) * ((y1 - y0) / (x1 - x0))
    }
}
