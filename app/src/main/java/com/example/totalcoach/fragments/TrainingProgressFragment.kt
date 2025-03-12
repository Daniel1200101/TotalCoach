package com.example.totalcoach.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.totalcoach.R

class TrainingProgressFragment : Fragment() {
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_training_progress, container, false)

        // Initialize WebView
        webView = view.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient() // Ensures that links open within the WebView

        // Set the fragment to have a menu option
        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_sheet_options, menu) // Inflate your menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sheet_option_1 -> {
                // Load the Google Sheets URL for "Trainee progress"
                webView.loadUrl("https://docs.google.com/spreadsheets/d/10mB_oxs-A5-DCs7BOgHRm2I_RhjNriYGqRAoM4dodBo/edit?usp=sharing")
                true
            }

            R.id.sheet_option_2 -> {
                // Load the Google Sheets URL for "Workout plan"
                webView.loadUrl("https://docs.google.com/spreadsheets/d/1D4T0I36JC1OIBNwnveb8fYa_CsEX7yd5EXOQSc8TfTA/edit?usp=sharing")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
