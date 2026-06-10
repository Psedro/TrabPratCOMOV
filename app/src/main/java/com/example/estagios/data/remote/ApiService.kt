package com.example.estagios.data.remote

import com.example.estagios.model.CandidatarOfertaResponse
import com.example.estagios.model.LoginRequest
import com.example.estagios.model.LoginResponse
import com.example.estagios.model.RegisterRequest
import com.example.estagios.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.estagios.model.CreateInternshipOfferRequest
import com.example.estagios.model.InternshipOfferResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {

    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @GET("roles")
    suspend fun getRoles(): List<RoleResponse>

    @GET("internship-offers")
    suspend fun getInternshipOffers(): Response<List<InternshipOfferResponse>>

    @GET("student-applications")
    suspend fun getStudentApplications(): List<StudentApplicationResponse>

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
}