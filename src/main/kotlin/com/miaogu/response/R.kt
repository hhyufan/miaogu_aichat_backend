package com.miaogu.response

data class R<T>(
    val code: Int,
    val msg: Map<String, String>? = null,
    val data: T? = null
) {
    companion object {
        fun <T> success(): R<T> {
            return R(ResponseCode.SUCCESS.code)
        }
        fun <T> success(msg: Pair<String, String>?): R<T> {
            return R(ResponseCode.SUCCESS.code,  msg?.let { mapOf( it.first to it.second) })
        }
        fun <T> success(msg: Pair<String, String>?, data: T): R<T> {
            return R(ResponseCode.SUCCESS.code, msg?.let { mapOf( it.first to it.second) }, data)
        }
        fun <T> success(msg: Map<String, String>?, data: T): R<T> {
            return R(ResponseCode.SUCCESS.code,  msg, data)
        }

        fun <T> success(data: T): R<T> {
            return R(ResponseCode.SUCCESS.code,mapOf( "success" to ResponseCode.SUCCESS.msg), data)
        }

        fun <T> fail(): R<T> {
            return R(ResponseCode.FAIL.code)
        }

        fun <T> fail(msg: Pair<String, String>?): R<T> {
            return R(ResponseCode.FAIL.code, msg?.let { mapOf( it.first to it.second) })
        }

        fun <T> fail(data: T): R<T> {
            return R(ResponseCode.FAIL.code, mapOf("error" to ResponseCode.FAIL.msg), data)
        }

        fun fail(code: Int, msg: Pair<String, String>?): R<Nothing> {
            return R(code  , msg?.let { mapOf( it.first to it.second) })
        }
        fun <T> fail(msg: Pair<String, String>?, data: T): R<T> {
            return R(ResponseCode.SUCCESS.code,  msg?.let { mapOf( it.first to it.second) }, data)
        }
    }
}
