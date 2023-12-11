package com.filledstacks.plugins.flutter_igolf_viewer.network

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log

import java.io.UnsupportedEncodingException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Auth {


    private val version = "1.1"
    private val signatureVersion = "2.0"
    private val signatureMthod = "HmacSHA256"
    private val responseFormat = "JSON"

    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("yyMMddHHmmssZZZZ")
    private val timestamp: String
        get() = timeFormat.format(Date())


    fun getUrlForRequest(request: String, apiKey: String, secretKey: String): String {
        val url1 = "${request}/${apiKey}/$version/$signatureVersion/$signatureMthod/"
        val url2 = "$timestamp/$responseFormat"
        val signature = makeSignature(url1 + url2, secretKey)
        return "$url1$signature/$url2"
    }

    private fun makeSignature(
        src: String,
        secret: String
    ): String? {
        val CHARACTER_ENCODING = "UTF-8"
        var res: String? = null
        try {
            val mac = Mac.getInstance("HMACSHA256")
            val sc = secret.toByteArray(charset(CHARACTER_ENCODING))
            mac.init(SecretKeySpec(sc, mac.algorithm))
            val bt = mac.doFinal(src.toByteArray(charset(CHARACTER_ENCODING)))
            res = Base64.encodeToString(bt, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            res = res!!.replace('+', '-').replace('/', '_')
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return res
    }

}