package com.example.eventosapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventosapp.R
import com.example.eventosapp.adapters.AttendeesAdapter
import com.example.eventosapp.adapters.CommentsAdapter
import com.example.eventosapp.databinding.ActivityEventDetailBinding
import com.example.eventosapp.models.Comment
import com.example.eventosapp.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var eventId: String
    private lateinit var commentsAdapter: CommentsAdapter
    private lateinit var attendeesAdapter: AttendeesAdapter
    private var isCreator = false
    private var isAttending = false

    // Propiedad para acceder más fácilmente al contenido incluido
    private val contentBinding get() = binding.contentEventDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la toolbar
        setSupportActionBar(binding.toolbar)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        eventId = intent.getStringExtra("eventId") ?: run {
            finish()
            return
        }

        setupRecyclerViews()
        loadEventData()
        setupCommentButton()
        setupAttendButton()
    }

    private fun setupRecyclerViews() {
        commentsAdapter = CommentsAdapter()
        attendeesAdapter = AttendeesAdapter()

        with(contentBinding) {
            recyclerViewComments.apply {
                layoutManager = LinearLayoutManager(this@EventDetailActivity)
                adapter = commentsAdapter
            }

            recyclerViewAttendees.apply {
                layoutManager = LinearLayoutManager(this@EventDetailActivity)
                adapter = attendeesAdapter
            }
        }
    }

    private fun loadEventData() {
        database.child("events").child(eventId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val event = snapshot.getValue(Event::class.java) ?: return
                    displayEventData(event)
                    isCreator = event.createdBy == auth.currentUser?.uid
                    invalidateOptionsMenu()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity,
                        "Error al cargar el evento", Toast.LENGTH_SHORT).show()
                }
            })

        database.child("event_attendees").child(eventId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attendees = mutableListOf<String>()
                    for (attendeeSnapshot in snapshot.children) {
                        attendeeSnapshot.key?.let { attendees.add(it) }
                    }
                    attendeesAdapter.submitList(attendees)

                    isAttending = attendees.contains(auth.currentUser?.uid)
                    updateAttendButton()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity,
                        "Error al cargar asistentes", Toast.LENGTH_SHORT).show()
                }
            })

        database.child("event_comments").child(eventId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<Comment>()
                    for (commentSnapshot in snapshot.children) {
                        commentSnapshot.getValue(Comment::class.java)?.let {
                            comments.add(it)
                        }
                    }
                    commentsAdapter.submitList(comments.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EventDetailActivity,
                        "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayEventData(event: Event) {
        with(contentBinding) {
            textViewEventName.text = event.name
            textViewEventDate.text = event.date
            textViewEventLocation.text = event.location
            layoutAddComment.visibility = if (isAttending) View.VISIBLE else View.GONE
        }
    }

    private fun setupCommentButton() {
        with(contentBinding) {
            buttonAddComment.setOnClickListener {
                val commentText = editTextComment.text.toString()
                if (commentText.isNotEmpty()) {
                    addComment(commentText)
                    editTextComment.text?.clear()
                }
            }
        }
    }

    private fun setupAttendButton() {
        contentBinding.buttonAttendEvent.setOnClickListener {
            toggleAttendance()
        }
    }

    private fun updateAttendButton() {
        with(contentBinding) {
            buttonAttendEvent.text = if (isAttending) "No asistiré" else "Asistiré"
            layoutAddComment.visibility = if (isAttending) View.VISIBLE else View.GONE
        }
    }

    private fun toggleAttendance() {
        val currentUserId = auth.currentUser?.uid ?: return
        val attendeeRef = database.child("event_attendees")
            .child(eventId)
            .child(currentUserId)

        if (isAttending) {
            attendeeRef.removeValue()
        } else {
            attendeeRef.setValue(true)
        }
    }

    private fun addComment(commentText: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val commentRef = database.child("event_comments")
            .child(eventId)
            .push()

        val comment = Comment(
            id = commentRef.key ?: "",
            text = commentText,
            userId = currentUserId,
            timestamp = System.currentTimeMillis()
        )

        commentRef.setValue(comment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isCreator) {
            menuInflater.inflate(R.menu.event_detail_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val intent = Intent(this, CreateEventActivity::class.java).apply {
                    putExtra("eventId", eventId)
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}