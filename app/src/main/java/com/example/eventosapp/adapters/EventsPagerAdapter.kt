package com.example.eventosapp.adapters

class EventsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MyEventsFragment()
            1 -> OtherEventsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}