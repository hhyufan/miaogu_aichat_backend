package com.miaogu.wrapper

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class CachedBodyHttpServletRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    // 读取请求体并缓存
    private val cachedBody: ByteArray = request.inputStream.readAllBytes()

    override fun getInputStream(): ServletInputStream {
        return CachedBodyServletInputStream(cachedBody)
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(ByteArrayInputStream(cachedBody), characterEncoding))
    }

    private class CachedBodyServletInputStream(cachedBody: ByteArray) : ServletInputStream() {

        private val buffer = ByteArrayInputStream(cachedBody)

        override fun read(): Int {
            return buffer.read()
        }

        override fun isFinished(): Boolean {
            return buffer.available() == 0
        }

        override fun isReady(): Boolean {
            return true
        }

        override fun setReadListener(listener: ReadListener?) {
            throw UnsupportedOperationException("Not implemented")
        }
    }
}
