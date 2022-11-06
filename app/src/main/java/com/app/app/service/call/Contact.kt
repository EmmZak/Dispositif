package com.app.app.service.call

enum class Contact(val fullNumber: String, val plusNumber: String, val tenDigitNumber: String) {
    Emmanuel("0033766006439", "+33766006439","0766006439"),
    Alexandre("0033621585966", "+33621585966", "0621585966"),
    Alexandre2("0033751005489", "+33751005489","0751005489");

    companion object {
        fun getContactNameByNumber(number: String): String {
            enumValues<Contact>().forEach { c ->
                if (number == c.fullNumber || number == c.tenDigitNumber || number == c.plusNumber) {
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