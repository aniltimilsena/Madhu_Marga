package com.smarthive.manager.data.remote.gemini

import com.smarthive.manager.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import com.smarthive.manager.utils.RateLimiter

@Singleton
class GeminiRepository @Inject constructor() {
    private val rateLimiter = RateLimiter(3000L) // 3 seconds for Gemini API
    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GeminiApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getFeedback(prompt: String): Result<String> {
        if (!rateLimiter.canExecute()) {
            return Result.failure(Exception("Rate limit exceeded. Please wait 3 seconds."))
        }
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = prompt)))
                )
            )
            val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val feedback = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response received"
            Result.success(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
