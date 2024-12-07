package com.example.eventosapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.eventosapp.R
import com.example.eventosapp.databinding.ActivityMainBinding
import com.example.eventosapp.fragments.MyEventsFragment
import com.example.eventosapp.fragments.OtherEventsFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupViewPager()
    }

    private fun setupViewPager() {
        // Inicializamos el adaptador y lo conectamos al ViewPager2
        val pagerAdapter = EventsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Configuramos los títulos de las pestañas
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "Mis Eventos"
                1 -> "Otros Eventos"
                else -> ""
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// El adaptador ahora especifica explícitamente que devuelve Fragment
private class EventsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {  // Cambiamos el tipo de retorno a Fragment o Any y no se puede resolver
        return when(position) {
            0 -> MyEventsFragment()
            1 -> OtherEventsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}