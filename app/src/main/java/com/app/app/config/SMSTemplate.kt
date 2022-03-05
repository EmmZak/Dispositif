package com.app.app.config

enum class SMSTemplate(val text: String) {
    OK("Salut, tout va bien"),
    KO("Salut, j'ai un souci, appelle-moi dès que tu peux"),
    ALERT("Alerte SOS de %s %s"),
    SOS("Num Sécu: %s, Médecin-%s, Localisation-%s"),
    LOCATION("Localisation {}")
}