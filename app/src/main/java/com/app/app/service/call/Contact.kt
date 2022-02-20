package com.app.app.service.call

enum class Contact(val number: String) {
    Emmanuel("0766006439"),
    Alexandre("0621585966");

    companion object {
        fun getAll(): Array<String> {
            return arrayOf(
                Emmanuel.number,
                Alexandre.number
            )
        }
    }

}