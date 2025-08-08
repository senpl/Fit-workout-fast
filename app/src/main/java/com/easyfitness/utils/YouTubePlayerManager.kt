package com.easyfitness.utils

import android.view.Gravity
import com.easyfitness.programs.ProgramRunner
import com.onurkaganaldemir.ktoastlib.KToast

fun removePlaylistFromYoutubeUrl(
    programRunner: ProgramRunner,
    videoUrl: String): YouTubeVideoDetails {
    if (videoUrl.isNotEmpty()) {
        var urlForTimestampExtraction = videoUrl
        if (videoUrl.contains("&list")) {
            val listParamIndex = urlForTimestampExtraction.indexOf("&list=")
            if (listParamIndex != -1) {
                val endOfListParam = urlForTimestampExtraction.indexOf('&', listParamIndex + 1)
                urlForTimestampExtraction = if (endOfListParam != -1) {
                    urlForTimestampExtraction.substring(0, listParamIndex) + urlForTimestampExtraction.substring(endOfListParam)
                } else {
                    urlForTimestampExtraction.substring(0, listParamIndex)
                }
            }
            val indexParamIndex = urlForTimestampExtraction.indexOf("&index=")
            if (indexParamIndex != -1) {
                val endOfIndexParam = urlForTimestampExtraction.indexOf('&', indexParamIndex + 1)
                urlForTimestampExtraction = if (endOfIndexParam != -1) {
                    urlForTimestampExtraction.substring(0, indexParamIndex) + urlForTimestampExtraction.substring(endOfIndexParam)
                } else {
                    urlForTimestampExtraction.substring(0, indexParamIndex)
                }
            }
            var startTime = 0
            if (videoUrl.contains("t=")) {
                var timeString = videoUrl.substring(videoUrl.lastIndexOf("t=") + "t=".length)
                if (timeString.contains('s')) {
                    timeString = timeString.substring(0, timeString.lastIndexOf('s'))
                }
                try {
                    val re = Regex("[^0-9 ]")
                    val onlySeconds = re.replace(timeString, "")
                    startTime = Integer.parseInt(onlySeconds)
                } catch (_: NumberFormatException) {
                    KToast.infoToast(
                        programRunner.requireActivity(),
                        "Failed to convert string to number:$timeString",
                        Gravity.BOTTOM,
                        KToast.LENGTH_LONG
                    )

                }

            }
            return YouTubeVideoDetails(
                urlForTimestampExtraction,
                startTime
            ) // Return the pair
        }
    }
    return YouTubeVideoDetails(videoUrl, 0)
}

data class YouTubeVideoDetails(var videoHash: String, val startTime: Int)
