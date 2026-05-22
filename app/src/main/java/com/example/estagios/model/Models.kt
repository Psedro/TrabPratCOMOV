package com.example.estagios.model

data class Oferta(
    val id: Int,
    val titulo: String,
    val empresa: String,
    val descricao: String,
    val tags: List<String>,
    val localizacao: String,
    val duracao: String,
    val vagas: String
)

data class Candidatura(
    val id: Int,
    val oferta: Oferta,
    val estado: EstadoCandidatura,
    val dataEnvio: String,
    val cvNome: String
)

enum class EstadoCandidatura(val label: String) {
    ACEITE("Aceite"),
    PENDENTE("Pendente"),
    EM_PROGRESSO("Em progresso")
}

data class Mensagem(
    val id: Int,
    val texto: String,
    val hora: String,
    val isEnviada: Boolean
)

// Sample data
object SampleData {
    val ofertas = listOf(
        Oferta(
            id = 1,
            titulo = "Desenvolvedor Full Stack",
            empresa = "TechCorp Solutions",
            descricao = "Estágio em desenvolvimento web com foco em React e Node.js. O estagiário irá trabalhar em projetos reais da empresa, desenvolvendo funcionalidades para o mundo de trabalho.",
            tags = listOf("React", "Node.js", "React"),
            localizacao = "Viana do Castelo",
            duracao = "6 Meses",
            vagas = "2 pessoas"
        ),
        Oferta(
            id = 2,
            titulo = "Designer UI/UX",
            empresa = "IPVC-ESTG",
            descricao = "Oportunidade para trabalhar em projetos de design de interfaces e experiência do utilizador. Trabalho em equipa multidisciplinar.",
            tags = listOf("React", "Node.js", "React"),
            localizacao = "Viana do Castelo",
            duracao = "6 Meses",
            vagas = "2 pessoas"
        )
    )

    val candidaturas = listOf(
        Candidatura(
            id = 1,
            oferta = ofertas[0],
            estado = EstadoCandidatura.ACEITE,
            dataEnvio = "14/02/2026",
            cvNome = "CV_PedroSousa"
        ),
        Candidatura(
            id = 2,
            oferta = ofertas[1],
            estado = EstadoCandidatura.PENDENTE,
            dataEnvio = "14/02/2026",
            cvNome = "CV_PedroSousa"
        )
    )

    val mensagens = listOf(
        Mensagem(1, "Podes enviar o relatório do estágio por favor.", "10:10", false),
        Mensagem(2, "Sim!", "10:10", true),
        Mensagem(3, "Estou só a ajustar uns pormenores.", "10:11", true),
        Mensagem(4, "Sem problema, quando acabares envia me por favor.", "10:11", false),
        Mensagem(5, "E envia para a tua professora também.", "10:11", false),
        Mensagem(6, "ok", "10:12", true)
    )
}
