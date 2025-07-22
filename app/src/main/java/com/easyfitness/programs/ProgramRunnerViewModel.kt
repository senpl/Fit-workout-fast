package com.easyfitness.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyfitness.DAO.ExerciseInProgram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramRunnerViewModel(
    // ... other DAOs or repositories
) : ViewModel() {

    private val _currentExercise =
        MutableStateFlow<ExerciseInProgram?>(null) // Replace Exercise with your model
    val currentExercise: StateFlow<ExerciseInProgram?> = _currentExercise.asStateFlow()

    private val _videoUrl = MutableStateFlow<String?>(null)
    val videoUrl: StateFlow<String?> = _videoUrl.asStateFlow()

    private val _showVideoPlayer = MutableStateFlow(false)
    val showVideoPlayer: StateFlow<Boolean> = _showVideoPlayer.asStateFlow()

    // Function to load an exercise and its video
    fun loadExercise(exerciseId: Long) {
        viewModelScope.launch {
            // Fetch exercise details from DAO
            // val exerciseDetails = daoMachine.getMachineById(exerciseId) // Example
            // _currentExercise.value = exerciseDetails
            // _videoUrl.value = extractYouTubeHash(exerciseDetails?.videoLink) // Your existing logic

            // Decide when to show the player
            // For example, if a video URL is available, prepare to show it
            _showVideoPlayer.value = _videoUrl.value?.isNotEmpty() == true
        }
    }

    fun toggleVideoPlayer() {
        _showVideoPlayer.value = !_showVideoPlayer.value
    }

    fun setVideoUrl(url: String?) {
        _videoUrl.value = url
        _showVideoPlayer.value = url?.isNotEmpty() == true
    }

    // Your existing PlayTube logic (video hash extraction, start time) can be refactored here
    // or as utility functions called from the ViewModel or Composable.
    fun getVideoHashAndStartTime(fullUrl: String?): Pair<String?, Int> {
        if (fullUrl.isNullOrEmpty()) return null to 0
        // ... (Your logic from PlayTube to extract hash and start time) ...
        // This is a simplified placeholder
        var videoHash = fullUrl
        if (fullUrl.contains("?v=")) {
            videoHash = fullUrl.substring(fullUrl.indexOf("?v=") + "?v=".length)
        }
        if (videoHash.contains("&")) {
            videoHash = videoHash.substring(0, videoHash.indexOf("&"))
        }
        var startTime = 0
        if (fullUrl.contains("t=")) {
            var timeString = fullUrl.substring(fullUrl.lastIndexOf("t=") + "t=".length)
            if (timeString.contains('s')) {
                timeString = timeString.substring(0, timeString.lastIndexOf('s'))
            }
            try {
                startTime = timeString.filter { it.isDigit() }.toIntOrNull() ?: 0
            } catch (_: NumberFormatException) { /* Handle error */ }
        }
        return videoHash to startTime
    }
}
