import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // For shared ViewModel with Activity scope
import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.Program // Your Program class
import com.easyfitness.databinding.TabProgramSelectBinding
import com.easyfitness.programs.ProgramListAdapter
import com.easyfitness.programs.ProgramWorkflowViewModel
import com.easyfitness.programs.ProgramWorkflowViewModelFactory
import com.easyfitness.programs.ProgramsFragmentDirections
//import com.fitworkoutfast.databinding.FragmentProgramSelectBinding // Your view binding class
// import YourProgramListAdapter

class ProgramSelectFragment : Fragment() {

    private var _binding: TabProgramSelectBinding? = null
    private val binding get() = _binding!!

    private lateinit var daoProgram: DAOProgram
    // Shared ViewModel scoped to the Activity
    private val sharedViewModel: ProgramWorkflowViewModel
    by activityViewModels {
        // You'll need a ViewModelFactory if your ViewModel has constructor dependencies
//        ProgramWorkflowViewModel(daoProgram)
        ProgramWorkflowViewModelFactory(requireContext()) // Example factory
    }
    private lateinit var programListAdapter: ProgramListAdapter // Replace with your adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabProgramSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        sharedViewModel.availablePrograms.observe(viewLifecycleOwner) { programs ->
            programListAdapter.submitList(programs) // Assuming ListAdapter
        }

        // Optional: If the Activity isn't handling navigation based on ViewModel event
        sharedViewModel.navigateToRunner.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { programId ->
                // Navigate to ProgramRunnerFragment, passing programId
                val action = ProgramsFragmentDirections.actionProgramSelectFragmentToProgramRunnerFragment(programId)
                findNavController().navigate(action)
            }
        }


        if (sharedViewModel.availablePrograms.value.isNullOrEmpty()) {
            sharedViewModel.loadAvailablePrograms()
        }
    }

    private fun setupRecyclerView() {
        programListAdapter =
            ProgramListAdapter { clickedProgram -> // Pass the click listener lambda
                sharedViewModel.programSelected(clickedProgram)
                // Navigation is handled by observing sharedViewModel.navigateToRunner or can be direct
            }
        binding.programsRecyclerView.apply {
            adapter = programListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
