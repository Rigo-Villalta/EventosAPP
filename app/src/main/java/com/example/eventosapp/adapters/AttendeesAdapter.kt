package com.example.eventosapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eventosapp.databinding.ItemAttendeeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AttendeesAdapter : ListAdapter<String, AttendeesAdapter.AttendeeViewHolder>(AttendeeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeViewHolder {
        val binding = ItemAttendeeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttendeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AttendeeViewHolder(
        private val binding: ItemAttendeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(userId: String) {
            // Cargar el email del usuario desde Firebase
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
                .child("email")
                .get()
                .addOnSuccessListener { snapshot ->
                    binding.textViewAttendeeEmail.text = snapshot.value as? String ?: "Usuario desconocido"
                }
        }
    }

    private class AttendeeDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}