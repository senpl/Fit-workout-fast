//package com.easyfitness.programs
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.easyfitness.DAO.DAOExerciseInProgram
//import com.easyfitness.DAO.DAOMachine
//import com.easyfitness.DAO.DAOProgram
//import com.easyfitness.DAO.DAORecord
//import com.easyfitness.DAO.Program
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import android.util.Log // Make sure Log is imported
//import androidx.compose.animation.core.copy
//import kotlinx.coroutines.flow.update
//
//class ProgramRunnerComposeViewModel(
//    private val daoProgram: DAOProgram,
//    private val daoMachine: DAOMachine,
//    private val daoExerciseInProgram: DAOExerciseInProgram, // Assuming this exists for exercises in a program
//    private val daoRecord: DAORecord // If needed for saving progress
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ProgramRunnerUiState(isLoading = true))
//    val uiState: StateFlow<ProgramRunnerUiState> = _uiState.asStateFlow()
//
//    // Example: Fetching programs
//    init {
//        Log.d("ViewModelLifecycle", "ViewModel initialized. Loading programs.")
//        loadPrograms()
//        // Initialize other things like units from shared preferences if needed
//        // For example:
//        // _uiState.update { it.copy(weightUnit = getWeightUnitPreference()) }
//    }
//
//    private fun loadPrograms() {
//        viewModelScope.launch {
//            Log.d("ViewModelLoad", "loadPrograms started. Current selected in state: ${_uiState.value.selectedProgram?.programName}")
//            _uiState.update { it.copy(isLoading = true) }
//            try {
//                val programsList = daoProgram.getAllPrograms()
//                Log.d("ViewModelLoad", "Fetched programsList. Count: ${programsList.size}")
//
//                if (programsList.isNotEmpty()) {
//                    val currentSelectedProgramInState = _uiState.value.selectedProgram
//                    var programToActuallySelect: com.easyfitness.DAO.Program? = null // Use the actual Program object
//
//                    // Update the programs list in the UI state FIRST
//                    // This is important so selectProgram can find it in the new list if needed
//                    _uiState.update { it.copy(programs = programsList, isLoading = false) }
//
//
//                    // Scenario 1: No program was selected before, or the list was empty. Select the first.
//                    if (currentSelectedProgramInState == null) {
//                        programToActuallySelect = programsList.first()
//                        Log.d("ViewModelLoad", "No program was selected. Selecting first: ${programToActuallySelect?.programName}")
//                    }
//                    // Scenario 2: A program WAS selected. Check if it's still in the new list.
//                    else {
//                        val previouslySelectedStillExists = programsList.find { it!!.key == currentSelectedProgramInState.key }
//                        if (previouslySelectedStillExists != null) {
//                            // The previously selected program is still valid.
//                            // We want to KEEP this selection, NOT override it with the first.
//                            // We also want to use the instance from the NEW list to avoid stale objects.
//                            programToActuallySelect = previouslySelectedStillExists
//                            Log.d("ViewModelLoad", "Previously selected program '${currentSelectedProgramInState.programName}' still exists. Re-confirming selection with new instance: ${programToActuallySelect.programName}")
//                        } else {
//                            // The previously selected program is NO LONGER in the list. Select the first from the new list.
//                            programToActuallySelect = programsList.first()
//                            Log.d("ViewModelLoad", "Previously selected program '${currentSelectedProgramInState.programName}' no longer in list. Selecting first: ${programToActuallySelect!!.programName}")
//                        }
//                    }
//
//                    // If we've determined a program to select (either new first, or re-confirming existing)
//                    if (programToActuallySelect != null) {
//                        // Call selectProgram ONLY IF the determined programToActuallySelect
//                        // is DIFFERENT from what's already in _uiState.value.selectedProgram
//                        // OR if exercises might need a refresh even for the same program.
//                        // For simplicity and robustness, let's call it if programToActuallySelect is not null
//                        // and selectProgram can handle being called with the same ID.
//                        // The crucial part is that programToActuallySelect here RESPECTS user's choice if valid.
//
//                        // Check if what we intend to select is already selected (by object reference or ID)
//                        if (_uiState.value.selectedProgram?.key != programToActuallySelect.key) {
//                            Log.d("ViewModelLoad", "Calling selectProgram for ID: ${programToActuallySelect.key} because it's different or needs refreshing.")
//                            selectProgram(programToActuallySelect.key)
//                        } else {
//                            // If it's the same program ID, we might still want to update the instance
//                            // to the one from the new programsList to avoid stale data,
//                            // without re-fetching exercises if not needed.
//                            if (_uiState.value.selectedProgram != programToActuallySelect) {
//                                _uiState.update { it.copy(selectedProgram = programToActuallySelect, isLoading = false) } // Ensure isLoading is false
//                                Log.d("ViewModelLoad", "Updated selectedProgram instance in state to new list's instance for ID: ${programToActuallySelect.key}, without calling full selectProgram.")
//                            } else {
//                                Log.d("ViewModelLoad", "Program ID ${programToActuallySelect.key} is already correctly selected. No change needed from loadPrograms.")
//                                _uiState.update { it.copy(isLoading = false) } // Still ensure loading is off
//                            }
//                        }
//                    }
//                    // If programToActuallySelect is null here, it means programsList was empty (handled below)
//
//                } else { // programsList is empty
//                    Log.d("ViewModelLoad", "No programs found in database.")
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            programs = emptyList(),
//                            selectedProgram = null,
//                            exercisesInProgram = emptyList(),
//                            currentExercise = null
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("ViewModelLoad", "Error loading programs: ${e.message}", e)
//                _uiState.update { it.copy(isLoading = false) }
//            }
//        }
//    }
//
//    fun selectProgram(programId: Long) {
//        viewModelScope.launch {
//            var programToSelect = _uiState.value.programs.find { it.id == programId }
//
//            if (programToSelect == null) {
//                Log.w("ViewModelSelect", "Program with ID $programId not found in uiState.programs. Fetching from DAO.")
//                programToSelect = daoProgram.getRecord(programId) // Fallback to DAO if not in the current list
//                if (programToSelect != null) {
//                    Log.d("ViewModelSelect", "Program ID $programId fetched from DAO: ${programToSelect.programName}")
//                }
//            } else {
//                Log.d("ViewModelSelect", "Program with ID $programId found in uiState.programs: ${programToSelect.programName}")
//            }
//
//                if (programToSelect != null) {
//                    // *** THIS IS THE CRITICAL PART FOR LOADING EXERCISES ***
//                    Log.d("ViewModelSelect", "Fetching exercises for SELECTED program: Name='${programToSelect.programName}', ID='${programToSelect.key}'")
//                    val exercisesForSelectedProgram = daoExerciseInProgram.getAllExerciseInProgram(programToSelect.key) // Use programToSelect.id
//                    Log.d("ViewModelSelect", "Exercises fetched for '${programToSelect.programName}'. Count: ${exercisesForSelectedProgram.size}")
//
//                    _uiState.update { currentState ->
//                        currentState.copy(
//                            isLoading = false,
//                            selectedProgram = programToSelect, // Set this as the currently selected program
//                            exercisesInProgram = exercisesForSelectedProgram, // Populate with exercises for THIS program
//                            currentExerciseOrder = 0, // Reset to the first exercise of the NEW program
//                            currentExercise = exercisesForSelectedProgram.firstOrNull(), // Set to the first exercise
//                            // Potentially clear other exercise-specific input fields here if needed:
//                            repsForCurrentExercise = "", // Example
//                            weightForCurrentExercise = "", // Example
//                            notesForCurrentExercise = "" // Example
//                        )
//                    }
//                    Log.d("ViewModelSelect", "UIState updated. New selectedProgram in state: ${_uiState.value.selectedProgram?.programName} (ID: ${_uiState.value.selectedProgram?.key})")
//                    Log.d("ViewModelSelect", "Exercises in state count: ${_uiState.value.exercisesInProgram.size}. First exercise: ${_uiState.value.currentExercise?.exerciseName}")
//
//
//                    updateVideoForCurrentExercise()
//                loadDataForCurrentExercise()
//            } else {
//                Log.w("ViewModelSelect", "Program with ID $programId not found for selection.")
//                _uiState.update { it.copy(isLoading = false) }
//            }
//        }
//    }
//
//    fun nextExercise() {
//        val currentOrder = _uiState.value.currentExerciseOrder
//        val exercises = _uiState.value.exercisesInProgram
//        if (currentOrder < exercises.size - 1) {
//            val newOrder = currentOrder + 1
//            _uiState.value = _uiState.value.copy(
//                currentExerciseOrder = newOrder,
//                currentExercise = exercises.getOrNull(newOrder)
//            )
//            updateVideoForCurrentExercise()
//            loadDataForCurrentExercise()
//        }
//    }
//
//    fun previousExercise() {
//        val currentOrder = _uiState.value.currentExerciseOrder
//        val exercises = _uiState.value.exercisesInProgram
//        if (currentOrder > 0) {
//            val newOrder = currentOrder - 1
//            _uiState.value = _uiState.value.copy(
//                currentExerciseOrder = newOrder,
//                currentExercise = exercises.getOrNull(newOrder)
//            )
//            updateVideoForCurrentExercise()
//            loadDataForCurrentExercise()
//        }
//    }
//
//    fun setCurrentExerciseByOrder(order: Int) {
//        val exercises = _uiState.value.exercisesInProgram
//        if (order >= 0 && order < exercises.size) {
//            _uiState.value = _uiState.value.copy(
//                currentExerciseOrder = order,
//                currentExercise = exercises.getOrNull(order)
//            )
//            updateVideoForCurrentExercise()
//            loadDataForCurrentExercise()
//        }
//    }
//
//
//    private fun updateVideoForCurrentExercise() {
//        val videoLink =
//            _uiState.value.currentExercise?.urlVideoStart // Adjust based on your data model
//        _uiState.value = _uiState.value.copy(videoUrl = videoLink)
//    }
//
//    fun toggleVideoPlayer() {
//        _uiState.value = _uiState.value.copy(showVideoPlayer = !_uiState.value.showVideoPlayer)
//    }
//
//    // --- Functions to handle data for the current exercise (notes, reps, weight) ---
//    private fun loadDataForCurrentExercise() {
//        viewModelScope.launch {
//            val currentEx = _uiState.value.currentExercise
//            if (currentEx != null) {
//                Log.d(
//                    "ViewModelData",
//                    "loadDataForCurrentExercise: ${currentEx.exerciseName} (ID: ${currentEx.id})"
//                )
//                // Fetch any specific persisted data for this currentEx.exerciseInProgramId if needed
//                // For now, we assume reps/weight/notes are transient UI state within the ViewModel
//                // If they were saved per exercise instance in a program, you'd load them here.
//                // For simplicity, if they are just part of the overall session, they might be reset
//                // when a new program or exercise is selected, as done in selectProgram.
//
//                // Example: If you saved the last used weight/reps for this specific exercise in this program context
//                // val record = daoRecord.getLastRecordForExerciseInProgram(currentEx.exerciseInProgramId)
//                // _uiState.update {
//                //    it.copy(
//                //        weightForCurrentExercise = record?.weight?.toString() ?: "",
//                //        repsForCurrentExercise = record?.reps?.toString() ?: "",
//                //        notesForCurrentExercise = record?.notes ?: ""
//                //    )
//                // }
//
//            } else {
//                Log.d("ViewModelData", "loadDataForCurrentExercise: No current exercise.")
//                _uiState.update {
//                    it.copy(
//                        repsForCurrentExercise = "",
//                        weightForCurrentExercise = "",
//                        notesForCurrentExercise = ""
//                    )
//                }
//            }
//        }
//    }
//
//    fun onNotesChanged(newNotes: String) {
//        _uiState.value = _uiState.value.copy(notesForCurrentExercise = newNotes)
//        // Optionally save to DB immediately or with a save button
//    }
//
//    fun onRepsChanged(newReps: String) {
//        _uiState.value = _uiState.value.copy(repsForCurrentExercise = newReps)
//    }
//
//    fun onWeightChanged(newWeight: String) {
//        _uiState.value = _uiState.value.copy(weightForCurrentExercise = newWeight)
//    }
//
//    fun saveCurrentExerciseDetails() {
//        viewModelScope.launch {
//            val exerciseToUpdate = _uiState.value.currentExercise
//            if (exerciseToUpdate != null) {
//                // Create an updated ExerciseInProgram object
//                val updatedExercise = exerciseToUpdate.copy( // LIKELY CULPRIT
//                    note = _uiState.value.notesForCurrentExercise,
//                    repetition = _uiState.value.repsForCurrentExercise.toIntOrNull()
//                        ?: exerciseToUpdate.repetition,
//                    poids = _uiState.value.weightForCurrentExercise.toFloatOrNull()
//                        ?: exerciseToUpdate.poids
//                )
//                // daoExerciseInProgram.update(updatedExercise) // Your DAO update method
//                // Potentially show a confirmation (Toast/Snackbar) via a new StateFlow event
//            }
//        }
//    }
//
//
//    // Add functions for saving records, handling settings, etc.
//}
