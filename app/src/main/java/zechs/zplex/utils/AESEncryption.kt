package zechs.zplex.utils

import zechs.zplex.utils.Constants.ENCRYPTION_KEY
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Suppress("SpellCheckingInspection")
object AESEncryption {

    // base64 encode of AiF4sa12SAfvlhiWu
    private const val SALT = "QWlGNHNhMTJTQWZ2bGhpV3U"

    // base64 encode of mT34SaFDASD8QAZX
    private const val IV = "bVQzNFNhRkRBU0Q4UUFaWA"

    private val ivParameterSpec = IvParameterSpec(Base64.getDecoder().decode(IV))
    private val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

    private val spec = PBEKeySpec(
        ENCRYPTION_KEY.toCharArray(),
        Base64.getDecoder().decode(SALT),
        65536, 256
    )

    fun encrypt(strToEncrypt: String): String? {
        try {
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

            return Base64.getEncoder().encodeToString(
                cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8))
            )
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt: String): String? {
        try {
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }

}