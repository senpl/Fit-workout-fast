package com.easyfitness.programs // Or your appropriate package

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyfitness.DAO.DAOProgram // Assuming DAOProgram needs context

/**
 * ViewModel Factory for ProgramWorkflowViewModel.
 *
 * This factory is needed if ProgramWorkflowViewModel has constructor dependencies
 * that cannot be provided by the default ViewModelProvider.Factory.
 *
 * @param context The application context, typically required for creating DAO instances or other Android framework dependencies.
 */
class ProgramWorkflowViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramWorkflowViewModel::class.java)) {
            // If ProgramWorkflowViewModel needs DAOProgram, and DAOProgram needs context:
            val daoProgram = DAOProgram(context.applicationContext) // Use applicationContext to avoid leaks
            return ProgramWorkflowViewModel(daoProgram) as T
            // Or, if ProgramWorkflowViewModel directly needs context (less common for testability):
            // return ProgramWorkflowViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
