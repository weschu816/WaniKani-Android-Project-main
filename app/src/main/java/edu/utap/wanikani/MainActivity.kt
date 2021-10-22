package edu.utap.wanikani

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentTransaction
import edu.utap.wanikani.ui.MainFragment

class MainActivity : AppCompatActivity() {
    companion object {
        var globalDebug = false
    }
    private lateinit var homeFragment: MainFragment

    private fun initHomeFragment() {
        supportFragmentManager
            .beginTransaction()
            // No back stack for home
            .replace(R.id.main_frame, homeFragment)
            // TRANSIT_FRAGMENT_FADE calls for the Fragment to fade away
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        homeFragment = MainFragment.newInstance()
        initHomeFragment()
        /*
        if (savedInstanceState == null) {
            // XXX Write me: add fragments to layout, swipeRefresh
            supportFragmentManager.beginTransaction()
                .add(R.id.main_frame, homeFragment)
                .commit()

            viewModel.netRefresh()
        }

         */
    }


}