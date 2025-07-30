package com.easyfitness.programs // Or your appropriate package for adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easyfitness.DAO.Program // Your Program data class
import com.easyfitness.databinding.ItemProgramBinding

//import com.fitworkoutfast.databinding.ItemProgramBinding // Generated View Binding class for item_program.xml

class ProgramListAdapter(
    private val onItemClicked: (Program) -> Unit
) : ListAdapter<Program, ProgramListAdapter.ProgramViewHolder>(ProgramDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val binding = ItemProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgramViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        val program = getItem(position)
        holder.bind(program)
    }

    inner class ProgramViewHolder(private val binding: ItemProgramBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(program: Program) {
            binding.programNameTextView.text = program.programName
            // Example: Show description if it's not null or empty
//            if (!program.description.isNullOrEmpty()) { // Assuming Program has a 'description' field
//                binding.programDescriptionTextView.text = program.description
//                binding.programDescriptionTextView.visibility = android.view.View.VISIBLE
//            } else {
//                binding.programDescriptionTextView.visibility = android.view.View.GONE
//            }
        }
    }

    // DiffUtil helps ListAdapter determine changes in the list efficiently
    class ProgramDiffCallback : DiffUtil.ItemCallback<Program>() {
        override fun areItemsTheSame(oldItem: Program, newItem: Program): Boolean {
            return oldItem.id == newItem.id // Assuming 'id' is a unique identifier for Program
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Program, newItem: Program): Boolean {
            return oldItem == newItem // Relies on Program being a data class or having a proper equals()
        }
    }
}
