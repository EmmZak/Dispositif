package com.app.app.service.sms

enum class Template(text: String) {
    OK("Salut, tout va bien"),
    KO("Salut, j'ai un souci"),
    SOS("Alerte SOS " +
            "\n Dispositif," +
            "\n Nom: {} " +
            "\n Prénom: {} " +
            "\n Num Sécu: {} " +
            "\n Médecin traitemnt: {} " +
            "\n Localisation suivante : {} ")
}