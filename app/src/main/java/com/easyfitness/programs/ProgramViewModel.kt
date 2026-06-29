package com.easyfitness.programs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.easyfitness.DAO.DAOExerciseInProgram
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.ExerciseInProgram
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgramViewModel(application: Application) : AndroidViewModel(application) {

    private val daoProgram = DAOProgram(getApplication())
    private val daoExerciseInProgram = DAOExerciseInProgram(getApplication())

    private val _programs = MutableStateFlow<List<String>>(emptyList())
    val programs: StateFlow<List<String>> = _programs.asStateFlow()

    private val _selectedProgramId = MutableStateFlow<Long>(-1)
    val selectedProgramId: StateFlow<Long> = _selectedProgramId.asStateFlow()

    private val _selectedProgramName = MutableStateFlow<String?>(null)
    val selectedProgramName: StateFlow<String?> = _selectedProgramName.asStateFlow()

    private val _exercises = MutableStateFlow<List<ExerciseInProgram>>(emptyList())
    val exercises: StateFlow<List<ExerciseInProgram>> = _exercises.asStateFlow()

    private val _currentExerciseOrder = MutableStateFlow(0)
    val currentExerciseOrder: StateFlow<Int> = _currentExerciseOrder.asStateFlow()

    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    init {
        loadPrograms()
    }

    fun startWorkout() {
        _isWorkoutActive.value = true
    }

    fun stopWorkout() {
        _isWorkoutActive.value = false
    }

    fun loadPrograms() {
        _programs.value = daoProgram.allProgramsNames
    }

    fun selectProgram(name: String) {
        _selectedProgramName.value = name
        val program = daoProgram.getRecord(name)
        if (program != null) {
            _selectedProgramId.value = program.id
            loadExercises(program.id)
        }
    }

//    fun selectProgram(id: Long) {
//        _selectedProgramId.value = id
//        loadExercises(id)
//    }

    private fun loadExercises(programId: Long) {
        viewModelScope.launch {
            val list = daoExerciseInProgram.getAllExerciseInProgram(programId)
            _currentExerciseOrder.value = 0
            _exercises.value = list
        }
    }

    fun setCurrentExerciseOrder(order: Int) {
        _currentExerciseOrder.value = order
    }

    fun nextExercise() {
        if (_currentExerciseOrder.value < _exercises.value.size - 1) {
            _currentExerciseOrder.value++
        }
    }

    fun previousExercise() {
        if (_currentExerciseOrder.value > 0) {
            _currentExerciseOrder.value--
        }
    }
}
