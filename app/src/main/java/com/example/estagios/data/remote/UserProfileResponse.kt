package com.example.estagios.data.remote

data class UserProfileResponse(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nome: String? = null,
    val username: String? = null,
    val email: String? = null,
    val status: String? = null,
    val roleId: String? = null,
    val tipo: String? = null,
    val student: StudentProfileData? = null,
    val teacher: TeacherProfileData? = null,
    val company: CompanyProfileData? = null
)

data class StudentProfileData(
    val studentId: String? = null,
    val indexNumber: Int? = null,
    val studyYear: Int? = null,
    val degreeLevel: String? = null
)

data class TeacherProfileData(
    val teacherId: String? = null,
    val academicTitle: String? = null,
    val teacherNumber: Int? = null,
    val department: String? = null
)

data class CompanyProfileData(
    val companyId: String? = null,
    val name: String? = null,
    val website: String? = null,
    val description: String? = null
)