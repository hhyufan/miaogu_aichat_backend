package com.miaogu.util

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import java.util.Base64

object RSAUtils {
    fun decrypt(encryptedText: String, privateKey: String): String {
        require(encryptedText.length >= 6) { "加密数据长度不足" }

        // 分离加密数据和盐值
        val encryptedData = encryptedText.dropLast(6)

        // 解密
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding").apply {
            init(Cipher.DECRYPT_MODE, loadPrivateKey(privateKey))
        }
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun loadPrivateKey(key: String): PrivateKey {
        // PKCS#8 格式的私钥
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(key.normalizeKey()))
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }

    private fun String.normalizeKey(): String {
        return this.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .trim()
    }
}
