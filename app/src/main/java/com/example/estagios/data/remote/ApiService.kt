package com.example.estagios.data.remote

import com.example.estagios.model.CandidatarOfertaResponse
import com.example.estagios.model.CompanyDashboardStatsResponse
import com.example.estagios.model.LoginRequest
import com.example.estagios.model.LoginResponse
import com.example.estagios.model.RegisterRequest
import com.example.estagios.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import com.example.estagios.model.CreateInternshipOfferRequest
import com.example.estagios.model.InternshipOfferResponse
import com.example.estagios.model.StudentDashboardStatsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @GET("roles")
    suspend fun getRoles(): List<RoleResponse>

    @GET("internship-offers")
    suspend fun getInternshipOffers(): Response<List<InternshipOfferResponse>>

    @GET("student-applications")
    suspend fun getStudentApplications(
        @Query("userId") userId: String
    ): List<StudentApplicationResponse>

    @GET("company-applications")
    suspend fun getCompanyApplications(
        @Query("userId") userId: String
    ): List<StudentApplicationResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("internship-offers")
    suspend fun criarOferta(
        @Body body: CreateInternshipOfferRequest
    ): Response<Unit>

    @Multipart
    @POST("applications")
    suspend fun candidatarOferta(
        @Part("userId") userId: RequestBody,
        @Part("internshipOfferId") internshipOfferId: RequestBody,
        @Part("availableFrom") availableFrom: RequestBody,
        @Part cv: MultipartBody.Part
    ): Response<CandidatarOfertaResponse>

    @GET("student-dashboard-stats")
    suspend fun getStudentDashboardStats(
        @Query("userId") userId: String
    ): StudentDashboardStatsResponse

    @GET("company-dashboard-stats")
    suspend fun getCompanyDashboardStats(
        @Query("userId") userId: String
    ): CompanyDashboardStatsResponse

    @GET("company-offers")
    suspend fun getCompanyOffers(
        @Query("userId") userId: String
    ): Response<List<InternshipOfferResponse>>

    @PATCH("applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Path("id") applicationId: String,
        @Body request: UpdateApplicationStatusRequest
    ): UpdateApplicationStatusResponse

    @GET("applications/{id}/messages")
    suspend fun getApplicationMessages(
        @Path("id") applicationId: String,
        @Query("userId") userId: String
    ): List<ApplicationMessageResponse>

    @POST("applications/{id}/messages")
    suspend fun sendApplicationMessage(
        @Path("id") applicationId: String,
        @Body request: SendApplicationMessageRequest
    ): ApplicationMessageResponse

    @GET("messages/conversations")
    suspend fun getMessageConversations(
        @Query("userId") userId: String
    ): List<ApplicationConversationResponse>
}