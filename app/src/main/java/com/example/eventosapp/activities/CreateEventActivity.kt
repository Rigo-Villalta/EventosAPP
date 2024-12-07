package com.example.eventosapp.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventosapp.databinding.ActivityCreateEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import com.example.eventosapp.models.Event

class CreateEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEventBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        eventId = intent.getStringExtra("eventId")
        if (eventId != null) {
            title = "Editar Evento"
            loadEventData()
        } else {
            title = "Crear Evento"
        }

        // Configurar el selector de fecha
        binding.editTextEventDate.setOnClickListener {
            showDatePicker()
        }

        binding.buttonSaveEvent.setOnClickListener {
            saveEvent()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                // Formato de fecha: YYYY-MM-DD
                val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.editTextEventDate.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadEventData() {
        eventId?.let { id ->
            database.getReference("events").child(id)
                .get()
                .addOnSuccessListener { snapshot ->
                    val event = snapshot.getValue(Event::class.java)
                    event?.let {
                        binding.editTextEventName.setText(it.name)
                        binding.editTextEventDate.setText(it.date)
                        binding.editTextEventLocation.setText(it.location)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar el evento", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun saveEvent() {
        val name = binding.editTextEventName.text.toString()
        val date = binding.editTextEventDate.text.toString()
        val location = binding.editTextEventLocation.text.toString()

        if (name.isEmpty() || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: return
        val eventsRef = database.getReference("events")

        // Si no hay eventId, crear uno nuevo
        val eventRef = if (eventId == null) {
            eventsRef.push()
        } else {
            eventsRef.child(eventId!!)
        }

        val event = Event(
            id = eventRef.key ?: "",
            name = name,
            date = date,
            location = location,
            createdBy = currentUserId,
            timestamp = System.currentTimeMillis()
        )

        eventRef.setValue(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento guardado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el evento", Toast.LENGTH_SHORT).show()
            }
    }
}
