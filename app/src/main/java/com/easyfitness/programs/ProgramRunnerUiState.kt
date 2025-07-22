package com.easyfitness.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyfitness.DAO.DAOExerciseInProgram
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.ExerciseInProgram // Assuming this is your data class
import com.easyfitness.DAO.Program // Assuming this is your data class
import com.easyfitness.utils.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProgramRunnerUiState(
    val programs: List<Program> = ArrayList(),
    val selectedProgram: Program? = null,
    val exercisesInProgram: List<ExerciseInProgram> = emptyList(),
    val currentExercise: ExerciseInProgram? = null,
    val currentExerciseOrder: Int = 0, // 0-based index
    val videoUrl: String? = null,
    val showVideoPlayer: Boolean = false,
    val weightUnit: String = "kg", // "kg" or "lbs"
    val distanceUnit: Int = UnitConverter.UNIT_KM, // Or your default
    // Add other relevant UI state properties here
    val isLoading: Boolean = false,
    val notesForCurrentExercise: String = "",
    val repsForCurrentExercise: String = "", // Or Int
    val weightForCurrentExercise: String = "", // Or Float
)

