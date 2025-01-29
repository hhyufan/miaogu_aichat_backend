package com.miaogu.response
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
   val httpStatus: Number,
   val msg: String? = null,
   val data: T? = null,
   var extra: Map<String, Any>? = null
) {
   constructor(httpStatus: HttpStatus, msg: String? = null, data: T? = null) : this(httpStatus.value(), msg ?: httpStatus.reasonPhrase, data)
}
