package com.app.app.service.call

enum class Contact(val number: String) {
    Emmanuel("0033766006439"),
    Alexandre("0033621585966");

    companion object {
        fun getAllNumbers(): Array<String> {
            return arrayOf(
                Emmanuel.number,
                Alexandre.number
            )
        }
    }

}