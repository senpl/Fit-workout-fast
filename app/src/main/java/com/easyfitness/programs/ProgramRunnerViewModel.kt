package com.easyfitness.programs
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easyfitness.DAO.DAOProgram // Assuming your DAOProgram and Program classes
import com.easyfitness.DAO.Program
import kotlinx.coroutines.launch

class ProgramWorkflowViewModel(private val daoProgram: DAOProgram) : ViewModel() { // Renamed for clarity

    private val _availablePrograms = MutableLiveData<List<Program>>()
    val availablePrograms: LiveData<List<Program>> = _availablePrograms

    private val _selectedProgram = MutableLiveData<Program?>()
    val selectedProgram: LiveData<Program?> = _selectedProgram

    // To navigate from Select to Runner
    private val _navigateToRunner = MutableLiveData<Event<Long>>() // Event wrapper for navigation trigger
    val navigateToRunner: LiveData<Event<Long>> = _navigateToRunner


    fun loadAvailablePrograms() {
        viewModelScope.launch {
            _availablePrograms.postValue(daoProgram.allProgramsToView) // Or your method to get all
        }
    }

    fun programSelected(program: Program) {
        _selectedProgram.value = program
        _navigateToRunner.value = Event(program.id) // Trigger navigation with program ID
    }

    fun clearSelectedProgram() { // If user navigates back from runner to select
        _selectedProgram.value = null
    }

    // Helper class for single-time event for navigation
    open class Event<out T>(private val content: T) {
        var hasBeenHandled = false
            private set // Allow external read but not write

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }
        fun peekContent(): T = content
    }
}
