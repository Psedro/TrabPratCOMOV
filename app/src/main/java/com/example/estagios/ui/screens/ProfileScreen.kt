package com.example.estagios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estagios.data.remote.RetrofitClient
import com.example.estagios.data.remote.UpdateUserProfileRequest
import com.example.estagios.ui.common.ProfileTopBar
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userId: String,
    nomeUtilizador: String,
    tipoUtilizador: TipoUtilizador,
    onVoltar: () -> Unit,
    onPerfilAtualizado: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var indexNumber by remember { mutableStateOf("") }
    var studyYear by remember { mutableStateOf("") }
    var degreeLevel by remember { mutableStateOf("") }

    var academicTitle by remember { mutableStateOf("") }
    var teacherNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    var companyName by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        try {
            isLoading = true

            val profile = RetrofitClient.apiService.getUserProfile(userId)

            firstName = profile.firstName.orEmpty()
            lastName = profile.lastName.orEmpty()
            username = profile.username.orEmpty()
            email = profile.email.orEmpty()

            indexNumber = profile.student?.indexNumber?.toString().orEmpty()
            studyYear = profile.student?.studyYear?.toString().orEmpty()
            degreeLevel = profile.student?.degreeLevel.orEmpty()

            academicTitle = profile.teacher?.academicTitle.orEmpty()
            teacherNumber = profile.teacher?.teacherNumber?.toString().orEmpty()
            department = profile.teacher?.department.orEmpty()

            companyName = profile.company?.name.orEmpty()
            website = profile.company?.website.orEmpty()
            description = profile.company?.description.orEmpty()

            erro = null
        } catch (e: Exception) {
            erro = e.message ?: "Erro ao carregar perfil"
        } finally {
            isLoading = false
        }
    }

    fun guardarPerfil() {
        scope.launch {
            try {
                isSaving = true

                RetrofitClient.apiService.updateUserProfile(
                    userId = userId,
                    request = UpdateUserProfileRequest(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        email = email,

                        indexNumber = indexNumber.toIntOrNull(),
                        studyYear = studyYear.toIntOrNull(),
                        degreeLevel = degreeLevel,

                        academicTitle = academicTitle,
                        teacherNumber = teacherNumber.toIntOrNull(),
                        department = department,

                        companyName = companyName,
                        website = website,
                        description = description
                    )
                )

                val novoNome = when (tipoUtilizador) {
                    TipoUtilizador.EMPRESA -> companyName.ifBlank { firstName }
                    else -> "$firstName $lastName".trim()
                }

                onPerfilAtualizado(novoNome.ifBlank { nomeUtilizador })

                snackbarHostState.showSnackbar("Perfil atualizado com sucesso")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Erro ao atualizar perfil")
            } finally {
                isSaving = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ProfileTopBar(
                nome = "PERFIL",
                mostrarNotificacoes = false,
                onVoltar = onVoltar
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2B5CE6))
                    }
                }

                erro != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erro ao carregar perfil: $erro",
                            color = Color(0xFFB00020)
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDDDDDD))
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = obterInicialPerfil(
                                    tipoUtilizador = tipoUtilizador,
                                    firstName = firstName,
                                    companyName = companyName
                                ),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Gestão de perfil",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = tipoTextoPerfil(tipoUtilizador),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontSize = 13.sp,
                            color = Color(0xFF777777)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        ProfileSection(title = "Dados da conta") {
                            if (tipoUtilizador != TipoUtilizador.EMPRESA) {
                                CampoPerfil(
                                    label = "Nome",
                                    value = firstName,
                                    onValueChange = { firstName = it }
                                )

                                CampoPerfil(
                                    label = "Apelido",
                                    value = lastName,
                                    onValueChange = { lastName = it }
                                )
                            }

                            CampoPerfil(
                                label = "Username",
                                value = username,
                                onValueChange = { username = it }
                            )

                            CampoPerfil(
                                label = "Email",
                                value = email,
                                onValueChange = { email = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (tipoUtilizador) {
                            TipoUtilizador.ALUNO -> {
                                ProfileSection(title = "Dados de aluno") {
                                    CampoPerfil(
                                        label = "Número de aluno",
                                        value = indexNumber,
                                        onValueChange = { indexNumber = it }
                                    )

                                    CampoPerfil(
                                        label = "Ano de estudo",
                                        value = studyYear,
                                        onValueChange = { studyYear = it }
                                    )

                                    CampoPerfil(
                                        label = "Grau",
                                        value = degreeLevel,
                                        onValueChange = { degreeLevel = it }
                                    )
                                }
                            }

                            TipoUtilizador.DOCENTE -> {
                                ProfileSection(title = "Dados de docente") {
                                    CampoPerfil(
                                        label = "Título académico",
                                        value = academicTitle,
                                        onValueChange = { academicTitle = it }
                                    )

                                    CampoPerfil(
                                        label = "Número de docente",
                                        value = teacherNumber,
                                        onValueChange = { teacherNumber = it }
                                    )

                                    CampoPerfil(
                                        label = "Departamento",
                                        value = department,
                                        onValueChange = { department = it }
                                    )
                                }
                            }

                            TipoUtilizador.EMPRESA -> {
                                ProfileSection(title = "Dados da empresa") {
                                    CampoPerfil(
                                        label = "Nome da empresa",
                                        value = companyName,
                                        onValueChange = { companyName = it }
                                    )

                                    CampoPerfil(
                                        label = "Website",
                                        value = website,
                                        onValueChange = { website = it }
                                    )

                                    CampoPerfil(
                                        label = "Descrição",
                                        value = description,
                                        onValueChange = { description = it },
                                        singleLine = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { guardarPerfil() },
                            enabled = !isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B5CE6)
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Guardar alterações",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF2B5CE6)
        )

        Spacer(modifier = Modifier.height(12.dp))

        content()
    }
}

@Composable
fun CampoPerfil(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2B5CE6),
            unfocusedBorderColor = Color(0xFFE0E0E0)
        )
    )
}

private fun obterInicialPerfil(
    tipoUtilizador: TipoUtilizador,
    firstName: String,
    companyName: String
): String {
    val base = if (tipoUtilizador == TipoUtilizador.EMPRESA) {
        companyName
    } else {
        firstName
    }

    return base.trim().firstOrNull()?.uppercase() ?: "?"
}

private fun tipoTextoPerfil(tipoUtilizador: TipoUtilizador): String {
    return when (tipoUtilizador) {
        TipoUtilizador.ALUNO -> "Aluno"
        TipoUtilizador.DOCENTE -> "Docente"
        TipoUtilizador.EMPRESA -> "Empresa"
    }
}