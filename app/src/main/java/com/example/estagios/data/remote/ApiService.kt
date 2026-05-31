package com.example.estagios.data.remote

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
}