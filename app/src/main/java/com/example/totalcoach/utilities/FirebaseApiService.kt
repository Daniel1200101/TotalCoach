import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Define the request and response models
data class SendEmailRequest(
    val email: String,
    val code: String
)

data class SendEmailResponse(
    val success: Boolean,
    val error: String? = null
)

interface FirebaseApiService {
    @POST("sendEmail")
    suspend fun sendEmail(@Body requestBody: SendEmailRequest): Response<SendEmailResponse>
}
