package com.gifticon.manager.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GifticonAnalysisService {
    
    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>
    
    @POST("api/analyze")
    suspend fun analyzeImage(@Body request: AnalyzeImageRequest): Response<AnalyzeImageResponse>
}

data class HealthResponse(
    val success: Boolean,
    val data: HealthData,
    val timestamp: String
)

data class HealthData(
    val status: String,
    val timestamp: String,
    val version: String,
    val environment: String
)

data class AnalyzeImageRequest(
    val imageBase64: String,
    val mimeType: String? = null
)

data class AnalyzeImageResponse(
    val success: Boolean,
    val data: GifticonAnalysisData?,
    val error: String?,
    val confidence: Double?
)

data class GifticonAnalysisData(
    val brandName: String,
    val productName: String?,
    val expiryDate: String,
    val amount: Int,
    val balance: Int,
    val barcodeNumber: String?,
    val category: String,
    val purchaseDate: String?
) 