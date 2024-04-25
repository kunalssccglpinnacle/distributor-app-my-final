import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Define the GetKeyResponse class to match the JSON response
data class GetKeyResponse(val key: String)

// Define the SearchBarr1Request class to match the JSON request
data class SearchBarr1Request(val searchValue: String)

// Define the SearchBarr1Response class to match the JSON response
data class SearchBarr1Response(val result: String)

// Define the ApiService interface
interface ApiService {
    @GET("getKey/{barcode}")
    fun getKey(@Path("barcode") barcode: String): Call<GetKeyResponse>

    @POST("searchBarr1")
    fun searchBarr1(@Body requestBody: SearchBarr1Request): Call<SearchBarr1Response>
}

