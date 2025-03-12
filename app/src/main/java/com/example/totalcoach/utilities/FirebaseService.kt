import android.content.Context
import android.widget.Toast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseService(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://us-central1-<your-project-id>.cloudfunctions.net/") // Your Firebase Cloud Function base URL
        .addConverterFactory(GsonConverterFactory.create()) // Use Gson to parse JSON
        .build()

    private val firebaseApiService = retrofit.create(FirebaseApiService::class.java)

    // Function to send an email using the Firebase Cloud Function
    fun sendEmailToTrainee(traineeEmail: String, uniqueCode: String) {
        // Create the request body
        val requestBody = SendEmailRequest(traineeEmail, uniqueCode)

        // Make the request on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = firebaseApiService.sendEmail(requestBody)

                // Handle the response on the main thread
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val sendEmailResponse = response.body()
                        if (sendEmailResponse?.success == true) {
                            // Success message
                            Toast.makeText(context, "Email sent successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Error message
                            Toast.makeText(context, "Failed to send email: ${sendEmailResponse?.error}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Failed to call Firebase function
                        Toast.makeText(context, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle network or other errors
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
