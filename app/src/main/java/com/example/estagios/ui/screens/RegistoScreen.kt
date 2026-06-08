package com.example.estagios.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.estagios.model.RegisterRequest
import com.example.estagios.model.TipoUtilizadorRegisto
import com.example.estagios.model.StudentRegisterData
import com.example.estagios.model.ProfessorRegisterData
import com.example.estagios.model.EmpresaRegisterData

@Composable
fun RegistoScreen(
    onRegistar: (RegisterRequest) -> Unit,
    onVoltarLogin: () -> Unit
) {
    var tipoUtilizador by remember { mutableStateOf(TipoUtilizadorRegisto.ESTUDANTE) }

    // Dados comuns
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }

    // Dados de estudante
    var numeroAluno by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var ano by remember { mutableStateOf("") }

    // Dados de professor
    var numeroProfessor by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }

    // Dados de empresa
    var nomeEmpresa by remember { mutableStateOf("") }
    var websiteEmpresa by remember { mutableStateOf("") }
    var descricaoEmpresa by remember { mutableStateOf("") }

    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Criar conta",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Seleciona o tipo de utilizador",
            style = MaterialTheme.typography.titleMedium
        )

        TipoUtilizadorSelector(
            tipoSelecionado = tipoUtilizador,
            onTipoSelecionado = { tipoUtilizador = it }
        )

        HorizontalDivider()

        Text(
            text = "Dados da conta",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Primeiro nome") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Apelido") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nome de utilizador") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Palavra-passe") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar palavra-passe") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        when (tipoUtilizador) {
            TipoUtilizadorRegisto.ESTUDANTE -> {
                CamposEstudante(
                    numeroAluno = numeroAluno,
                    onNumeroAlunoChange = { numeroAluno = it },
                    curso = curso,
                    onCursoChange = { curso = it },
                    ano = ano,
                    onAnoChange = { ano = it }
                )
            }

            TipoUtilizadorRegisto.PROFESSOR -> {
                CamposProfessor(
                    numeroProfessor = numeroProfessor,
                    onNumeroProfessorChange = { numeroProfessor = it },
                    departamento = departamento,
                    onDepartamentoChange = { departamento = it }
                )
            }

            TipoUtilizadorRegisto.EMPRESA -> {
                CamposEmpresa(
                    nomeEmpresa = nomeEmpresa,
                    onNomeEmpresaChange = { nomeEmpresa = it },
                    websiteEmpresa = websiteEmpresa,
                    onWebsiteEmpresaChange = { websiteEmpresa = it },
                    descricaoEmpresa = descricaoEmpresa,
                    onDescricaoEmpresaChange = { descricaoEmpresa = it }
                )
            }
        }

        erro?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                erro = validarRegisto(
                    tipoUtilizador = tipoUtilizador,
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    email = email,
                    password = password,
                    confirmarPassword = confirmarPassword,
                    numeroAluno = numeroAluno,
                    curso = curso,
                    ano = ano,
                    numeroProfessor = numeroProfessor,
                    departamento = departamento,
                    nomeEmpresa = nomeEmpresa
                )

                if (erro == null) {
                    val pedidoRegisto = RegisterRequest(
                        nome = "$firstName $lastName".trim(),
                        email = email.trim(),
                        username = username.trim(),
                        password = password,
                        tipo = tipoUtilizador.valorApi,

                        estudante = if (tipoUtilizador == TipoUtilizadorRegisto.ESTUDANTE) {
                            StudentRegisterData(
                                numeroAluno = numeroAluno,
                                curso = curso,
                                ano = ano
                            )
                        } else null,

                        professor = if (tipoUtilizador == TipoUtilizadorRegisto.PROFESSOR) {
                            ProfessorRegisterData(
                                numeroProfessor = numeroProfessor,
                                departamento = departamento
                            )
                        } else null,

                        empresa = if (tipoUtilizador == TipoUtilizadorRegisto.EMPRESA) {
                            EmpresaRegisterData(
                                nomeEmpresa = nomeEmpresa,
                                website = websiteEmpresa,
                                descricao = descricaoEmpresa
                            )
                        } else null
                    )

                    onRegistar(pedidoRegisto)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar conta")
        }

        TextButton(
            onClick = onVoltarLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Já tenho conta")
        }
    }
}

@Composable
fun TipoUtilizadorSelector(
    tipoSelecionado: TipoUtilizadorRegisto,
    onTipoSelecionado: (TipoUtilizadorRegisto) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipoUtilizadorRegisto.values().forEach { tipo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = tipoSelecionado == tipo,
                    onClick = { onTipoSelecionado(tipo) }
                )

                Text(
                    text = tipo.label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun CamposEstudante(
    numeroAluno: String,
    onNumeroAlunoChange: (String) -> Unit,
    curso: String,
    onCursoChange: (String) -> Unit,
    ano: String,
    onAnoChange: (String) -> Unit
) {
    Text(
        text = "Dados do estudante",
        style = MaterialTheme.typography.titleMedium
    )

    OutlinedTextField(
        value = numeroAluno,
        onValueChange = onNumeroAlunoChange,
        label = { Text("Número de aluno") },
        leadingIcon = { Icon(Icons.Outlined.School, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = curso,
        onValueChange = onCursoChange,
        label = { Text("Curso") },
        leadingIcon = { Icon(Icons.Outlined.School, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = ano,
        onValueChange = onAnoChange,
        label = { Text("Ano") },
        leadingIcon = { Icon(Icons.Outlined.School, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun CamposProfessor(
    numeroProfessor: String,
    onNumeroProfessorChange: (String) -> Unit,
    departamento: String,
    onDepartamentoChange: (String) -> Unit
) {
    Text(
        text = "Dados do professor",
        style = MaterialTheme.typography.titleMedium
    )

    OutlinedTextField(
        value = numeroProfessor,
        onValueChange = onNumeroProfessorChange,
        label = { Text("Número de professor") },
        leadingIcon = { Icon(Icons.Outlined.School, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = departamento,
        onValueChange = onDepartamentoChange,
        label = { Text("Departamento / área") },
        leadingIcon = { Icon(Icons.Outlined.School, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun CamposEmpresa(
    nomeEmpresa: String,
    onNomeEmpresaChange: (String) -> Unit,
    websiteEmpresa: String,
    onWebsiteEmpresaChange: (String) -> Unit,
    descricaoEmpresa: String,
    onDescricaoEmpresaChange: (String) -> Unit
) {
    Text(
        text = "Dados da empresa",
        style = MaterialTheme.typography.titleMedium
    )

    OutlinedTextField(
        value = nomeEmpresa,
        onValueChange = onNomeEmpresaChange,
        label = { Text("Nome da empresa") },
        leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = websiteEmpresa,
        onValueChange = onWebsiteEmpresaChange,
        label = { Text("Website") },
        leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = descricaoEmpresa,
        onValueChange = onDescricaoEmpresaChange,
        label = { Text("Descrição da empresa") },
        leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}

fun validarRegisto(
    tipoUtilizador: TipoUtilizadorRegisto,
    firstName: String,
    lastName: String,
    username: String,
    email: String,
    password: String,
    confirmarPassword: String,
    numeroAluno: String,
    curso: String,
    ano: String,
    numeroProfessor: String,
    departamento: String,
    nomeEmpresa: String
): String? {
    if (firstName.isBlank()) return "O primeiro nome é obrigatório."
    if (lastName.isBlank()) return "O apelido é obrigatório."
    if (username.isBlank()) return "O nome de utilizador é obrigatório."
    if (email.isBlank()) return "O email é obrigatório."
    if (!email.contains("@")) return "O email não é válido."
    if (password.isBlank()) return "A palavra-passe é obrigatória."
    if (password.length < 6) return "A palavra-passe deve ter pelo menos 6 caracteres."
    if (password != confirmarPassword) return "As palavras-passe não coincidem."

    return when (tipoUtilizador) {
        TipoUtilizadorRegisto.ESTUDANTE -> {
            when {
                numeroAluno.isBlank() -> "O número de aluno é obrigatório."
                curso.isBlank() -> "O curso é obrigatório."
                ano.isBlank() -> "O ano é obrigatório."
                else -> null
            }
        }

        TipoUtilizadorRegisto.PROFESSOR -> {
            when {
                numeroProfessor.isBlank() -> "O número de professor é obrigatório."
                departamento.isBlank() -> "O departamento é obrigatório."
                else -> null
            }
        }

        TipoUtilizadorRegisto.EMPRESA -> {
            when {
                nomeEmpresa.isBlank() -> "O nome da empresa é obrigatório."
                else -> null
            }
        }
    }
}