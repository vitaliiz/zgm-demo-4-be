package com.example

import java.security.MessageDigest
import java.util.Base64


fun toHash(text: String): String {
    val sha256 = MessageDigest.getInstance("SHA-256")
    val hashBytes = sha256.digest(text.toByteArray())
    return Base64.getEncoder().encodeToString(hashBytes).replace("/", "_").replace("+", "-")
}