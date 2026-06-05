package com.example.estagios.data.remote

import com.example.estagios.model.LoginRequest
import com.example.estagios.model.LoginResponse
import com.example.estagios.model.RegisterRequest
import com.example.estagios.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @GET("roles")
    suspend fun getRoles(): List<RoleResponse>

    @GET("internship-offers")
    suspend fun getInternshipOffers(): List<InternshipOfferResponse>

    @POST("applications")
    suspend fun createApplication(
        @Body request: CreateApplicationRequest
    ): ApplicationResponse

    @GET("student-applications")
    suspend fun getStudentApplications(): List<StudentApplicationResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>
}