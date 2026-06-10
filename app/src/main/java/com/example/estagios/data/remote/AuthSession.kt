package com.example.estagios.data.remote

import com.example.estagios.model.LoggedUser

object AuthSession {
    var userId: String? = null
    var nome: String? = null
    var email: String? = null
    var tipo: String? = null

    fun iniciarSessao(user: LoggedUser) {
        userId = user.id
        nome = user.nome
        email = user.email
        tipo = user.tipo
    }

    fun terminarSessao() {
        userId = null
        nome = null
        email = null
        tipo = null
    }
}
