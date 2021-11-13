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
    private fun createSpeedArrayOfFirstRange(
        index2: Int,
        speed1: Float,
        speed2: Float
    ): FloatArray {
        val index1 = 0
        val firstRangeSpeedArray = FloatArray(index2 - index1 + 1)

        for (i in 0..index2) {
            firstRangeSpeedArray[i] = linearInterpolation(speed1, i, speed2, speed1, index2, index1)
        }

        return firstRangeSpeedArray
    }

    private fun createSpeedArrayOfSecondRange(
        index2: Int,
        speed2: Float,
        speed3: Float
    ): FloatArray {
        val index3 = Constants.ITEM_SIZE
        val secondRangeSpeedArray = FloatArray(index3 - index2)

        for (i in index2 + 1..index3) {
            secondRangeSpeedArray[i - (index2 + 1)] =
                linearInterpolation(speed2, (i - index2), speed3, speed2, index3, index2)
        }

        return secondRangeSpeedArray
    }

    private fun createSpeedArrayOfFrames(
        index2: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float
    ): FloatArray {
        // Since the mid value is included in both first range and second range, it is removed
        // from the second one
        return createSpeedArrayOfFirstRange(index2, speed1, speed2)
            .plus(removeTheFirstElement(createSpeedArrayOfSecondRange(index2, speed2, speed3)))
    }

    private fun createArrayOfFrameDisplayTime(
        index2: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float
    ): FloatArray {
        val index1 = 0
        val index3 = Constants.ITEM_SIZE - 1
        var frameDisplayTime: Float
        val frameDisplayTimeArray = FloatArray(index3 - index1 + 1)

        for ((index, value) in createSpeedArrayOfFrames(index2, speed1, speed2, speed3).toList()
            .withIndex()) {
            frameDisplayTime = 1 / value
            frameDisplayTimeArray[index] = frameDisplayTime
        }
        return frameDisplayTimeArray
    }

    private fun createListOfPrintNumbersOfFrames(
        index2: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float
    ): List<Int> {
        val printNumberList = arrayListOf<Int>()

        var remainder = 0.0f

        for (frameDisplayTime in createArrayOfFrameDisplayTime(index2, speed1, speed2, speed3)) {
            remainder += frameDisplayTime % 1
            var division: Int = (frameDisplayTime / 1).toInt()

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

    fun createProjectFrames(index2: Int, speed1: Float, speed2: Float, speed3: Float): IntArray {
        val projectFrames = arrayListOf<Int>()

        for ((index, value) in createListOfPrintNumbersOfFrames(
            index2,
            speed1,
            speed2,
            speed3
        ).withIndex()) {
            repeat(value) {
                projectFrames.add(index)
            }
        }
        return projectFrames.toIntArray()
    }

    fun generateFrames(
        startPosition: Int,
        speed1: Float,
        speed2: Float,
        speed3: Float,
        index2: Int
    ): Flow<FrameInfo> = flow {

        val projectFrames = createProjectFrames(index2, speed1, speed2, speed3)

        for (i in startPosition until projectFrames.size) {

            emit(FrameInfo(i, projectFrames[i]))
            delay(Constants.DELAY_TIME)
        }
    }

    private fun removeTheFirstElement(arr: FloatArray): FloatArray {
        return (arr.indices)
            .filter { i: Int -> i != 0 }
            .map { i: Int -> arr[i] }
            .toFloatArray()
    }

    private fun linearInterpolation(
        initialSpeed: Float, currentIndex: Int,
        y1: Float, y0: Float, x1: Int, x0: Int
    ): Float {
        return initialSpeed + currentIndex * ((y1 - y0) / (x1 - x0))
    }
}
