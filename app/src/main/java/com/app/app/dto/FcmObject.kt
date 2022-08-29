package com.app.app.dto

data class FcmObject(var to: String?, var data: FcmObjectData?) {
    constructor() : this(null, null)

    fun toJson(): String {
        return "{to: $to, data: ${data?.toJson()}}"
    }
}
