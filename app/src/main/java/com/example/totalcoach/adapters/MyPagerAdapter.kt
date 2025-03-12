package com.example.yourappname

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.totalcoach.TrainerMainActivity
import com.example.totalcoach.TraineeMainActivity
import com.example.totalcoach.fragments.CalendarFragment
import com.example.totalcoach.fragments.PaymentsFragment
import com.example.totalcoach.fragments.TaskListFragment
import com.example.totalcoach.fragments.TraineesFragment
import com.example.totalcoach.fragments.MediaFragment
import com.example.totalcoach.fragments.TrainingProgressFragment

// General PagerAdapter class that can be reused for both Trainer and Trainee
abstract class BasePagerAdapter(activity: androidx.fragment.app.FragmentActivity, private val traineeId: String? = null) : FragmentStateAdapter(activity) {
    private val fragmentList = mutableListOf<Fragment>()

    // Abstract function to get the initial set of fragments.
    abstract fun getFragments(): List<Fragment>

    init {
        // Initialize the fragment list with fragments provided by child classes.
        fragmentList.addAll(getFragments())
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = fragmentList[position]

        // Pass traineeId to fragments if it's not null
        if (traineeId != null) {
            Log.d("PagerAdapter", "Passing traineeId: $traineeId to ${fragment.javaClass.simpleName}")

            val args = fragment.arguments ?: Bundle()
            args.putString("traineeId", traineeId)
            fragment.arguments = args
        }

        return fragment
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    // Method to replace a fragment at a specific position
    fun replaceFragment(position: Int, newFragment: Fragment) {
        // Replace the fragment at the given position with the new fragment
        fragmentList[position] = newFragment
        notifyItemChanged(position)  // Notify adapter that the fragment has changed
    }

    // Method to add a new fragment
    fun addFragment(newFragment: Fragment) {
        fragmentList.add(newFragment)
        notifyItemInserted(fragmentList.size - 1)  // Notify adapter to add the fragment
    }

    // Method to remove a fragment at a specific position
    fun removeFragment(position: Int) {
        fragmentList.removeAt(position)
        notifyItemRemoved(position)  // Notify adapter that the fragment has been removed
    }
}


class TrainerPagerAdapter(activity: TrainerMainActivity) : BasePagerAdapter(activity) {
    override fun getFragments(): List<Fragment> {
        return listOf(
            TaskListFragment(), // TaskList
            TraineesFragment(), // Trainee
            CalendarFragment(), // Calendar
            PaymentsFragment() // Payments
        )
    }
}

class TraineePagerAdapter(activity: TraineeMainActivity, traineeId: String) : BasePagerAdapter(activity, traineeId) {
    override fun getFragments(): List<Fragment> {
        return listOf(
            TaskListFragment(), // TaskList
            MediaFragment(), // Pic and Video
            TrainingProgressFragment(), // Training Progress
            CalendarFragment() // Calendar
        )
    }
}

