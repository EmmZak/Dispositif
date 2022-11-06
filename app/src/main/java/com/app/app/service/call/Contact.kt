package com.app.app.service.call

enum class Contact(val fullNumber: String, val tenDigitNumber: String) {
    Emmanuel("0033766006439", "0766006439"),
    Alexandre("0033621585966", "0621585966");

    companion object {
        fun getContactNameByNumber(number: String): String {
            enumValues<Contact>().forEach { c ->
                if (number == c.fullNumber || number == c.tenDigitNumber) {
                    return c.name
                }
            }
            return "Inconnu"
        }
        fun getAllNumbers(): Array<String> {
            return arrayOf(
                Emmanuel.fullNumber,
                Alexandre.fullNumber
            )
        }
    }

}