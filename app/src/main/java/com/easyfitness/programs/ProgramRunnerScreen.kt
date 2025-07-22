package com.easyfitness.programs

import android.util.Log
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.SimpleYouTubePlayerOptionsBuilder
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubePlayer
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubePlayerHostState
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubePlayerState
import io.github.ilyapavlovskii.multiplatform.youtubeplayer.YouTubeVideoId
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun ProgramRunnerScreen(
    viewModel: ProgramRunnerViewModel = androidx.lifecycle.viewmodel.compose.viewModel() // Get ViewModel instance
) {
    val currentExercise by viewModel.currentExercise.collectAsStateWithLifecycle()
    val videoUrl by viewModel.videoUrl.collectAsStateWithLifecycle() // The raw URL or pre-processed hash
    val showVideoPlayer by viewModel.showVideoPlayer.collectAsStateWithLifecycle()

    val (youtubeVideoId, startTimeSeconds) = remember(videoUrl) { // Recalculate when videoUrl changes
        viewModel.getVideoHashAndStartTime(videoUrl)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top part of your UI (Exercise details, buttons, etc.)
        Text(text = "Current Exercise: ${currentExercise?.exercise ?: "None"}") // Example
        Button(onClick = {
            // Example: Load a test exercise
            // In a real app, this would be triggered by selecting an exercise
            viewModel.setVideoUrl("YOUR_YOUTUBE_VIDEO_URL_HERE?t=30s") // Example URL
        }) {
            Text("Load Example Video & Show Player")
        }

        Button(onClick = { viewModel.toggleVideoPlayer() }) {
            Text(if (showVideoPlayer) "Hide Player" else "Show Player")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Embedded Video Player Section ---
        if (showVideoPlayer && youtubeVideoId != null) {
            EmbeddedYouTubePlayer(
                youtubeVideoId = youtubeVideoId,
                startTimeSeconds = startTimeSeconds,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f) // Or fixed height: .height(300.dp)
            )
        } else if (showVideoPlayer && youtubeVideoId == null) {
            Text("Video URL is invalid or not available.")
        }

        // Rest of your ProgramRunner UI (e.g., record list, controls)
        // ...
        // Example: Displaying notes
        // currentExercise?.notes?.let { Text(text = "Notes: $it") }

        // Example: Your "Exercises List Button", "Next/Previous Arrows"
        // These would now call methods on your ViewModel
    }
}

@Composable
fun EmbeddedYouTubePlayer(
    youtubeVideoId: String,
    startTimeSeconds: Int,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val hostState = remember { YouTubePlayerHostState() } // From the YouTube player library

    // Observe player state for debugging or advanced controls if needed
    LaunchedEffect(hostState.currentState) {
        when (val state = hostState.currentState) {
            is YouTubePlayerState.Error -> Log.e("YouTubePlayer", "Error: ${state.message}")
            YouTubePlayerState.Idle -> Log.d("YouTubePlayer", "Idle")
            is YouTubePlayerState.Playing -> Log.d("YouTubePlayer", "Playing video ID: ${state.videoId}")
            YouTubePlayerState.Ready -> {
                Log.d("YouTubePlayer", "Ready to play. Loading video: $youtubeVideoId")
                coroutineScope.launch {
                    hostState.loadVideo(YouTubeVideoId(youtubeVideoId))
                    // Consider if seekTo should happen after a slight delay or based on another state
                }
            }
            // Add other states if necessary
        }
    }

    // Effect to seek when video ID or start time changes AFTER the player is ready
    // and has loaded the video. This can be tricky with `Ready` state.
    // A more robust way might involve observing a "video loaded" state.
    LaunchedEffect(hostState.currentState, youtubeVideoId, startTimeSeconds) {
        if (hostState.currentState is YouTubePlayerState.Playing || hostState.currentState == YouTubePlayerState.Ready) {
            // Check if the current video loaded is the one we want.
            // The YouTubePlayerHostState might not immediately reflect the videoId after loadVideo call.
            // This part might need refinement based on how the library handles state post-loadVideo.
            if ((hostState.currentState as? YouTubePlayerState.Playing)?.videoId?.id == youtubeVideoId ||
                hostState.currentState == YouTubePlayerState.Ready) { // Assuming Ready implies it will load the latest
                if (startTimeSeconds > 0) {
                    Log.d("YouTubePlayer", "Seeking to $startTimeSeconds seconds for $youtubeVideoId")
                    hostState.seekTo(startTimeSeconds.seconds)
                }
            }
        }
    }


    Column(modifier = modifier) {
        YouTubePlayer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Takes available space in the Column
            hostState = hostState,
            options = SimpleYouTubePlayerOptionsBuilder.builder {
                autoplay(true) // Autoplay might be desired if shown directly
                controls(true) // Show default controls
                rel(false)
                ivLoadPolicy(false)
                ccLoadPolicy(false)
                // fullscreen = true // Fullscreen might need special handling in Compose
            }
        )
        // You can add custom controls below if needed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { coroutineScope.launch { hostState.play() } }) { Text("Play") }
            Button(onClick = { coroutineScope.launch { hostState.pause() } }) { Text("Pause") }
            Button(onClick = { coroutineScope.launch { hostState.seekTo(startTimeSeconds.seconds) } }) { Text("Restart Section") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProgramRunnerScreen() {
    MaterialTheme { // Replace with YourAppTheme
        ProgramRunnerScreen(
            // You can create a fake ViewModel for previews if needed
        )
    }
}
