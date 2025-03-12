package com.example.totalcoach.utilities

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Locale

class AddressManager(context: Context) {

    private var placesClient: PlacesClient? = null

    init {
        // Initialize Places API if not initialized
        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, "AIzaSyBC27C3NRgsjWdtwu17jr281o7HdseERGU", Locale.getDefault())  // Replace with your actual API key
        }
        placesClient = Places.createClient(context)
    }

    // Fetches address suggestions based on user input
    fun getAddressSuggestions(query: String, callback: (List<String>) -> Unit) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient?.findAutocompletePredictions(request)?.addOnSuccessListener { response ->
            val suggestions = response.autocompletePredictions.map { prediction ->
                prediction.getFullText(null).toString()
            }
            callback(suggestions)
        }?.addOnFailureListener { exception ->
            Log.e("AddressManager", "Error fetching address suggestions: ${exception.message}")
            callback(emptyList())
        }
    }

    // Validates if the address is correct by checking if it can be resolved via Geocoder
    fun validateAddress(address: String, context: Context, callback: (Boolean) -> Unit) {
        val geocoder = Geocoder(context)
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses?.isNotEmpty() == true) {
                callback(true)  // Address is valid
            } else {
                callback(false)  // Invalid address
            }
        } catch (e: Exception) {
            Log.e("AddressManager", "Error validating address: ${e.message}")
            callback(false)
        }
    }

    // Extracts the city and country from a given address using Google Places API
    fun getCityAndCountryFromPlace(address: String, context: Context, callback: (String, String) -> Unit) {
        val placesClient = Places.createClient(context)
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(address)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                if (response.autocompletePredictions.isNotEmpty()) {
                    val prediction = response.autocompletePredictions[0]

                    // Initialize city and country variables
                    var city = ""
                    var country = ""

                    // Parse the place types and extract city and country
                    for (component in prediction.placeTypes) {
                        if (component == com.google.android.libraries.places.api.model.Place.Type.LOCALITY) {
                            city = prediction.getFullText(null).toString()
                        }
                        if (component == com.google.android.libraries.places.api.model.Place.Type.COUNTRY) {
                            country = prediction.getFullText(null).toString()
                        }
                    }

                    // Call the callback with the city and country
                    callback(city, country)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AddressManager", "Error retrieving city and country: ${exception.message}")
                callback("", "")
            }
    }

}
