package com.nbl.npa.Config;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class for AES-256 encryption and decryption. */
public final class AES256 {

    private static final Logger LOG = LoggerFactory.getLogger(AES256.class);

    private static final String PASSWORD;
    private static final String IV;
    private static final byte[] SALT;

    static {
        PASSWORD = System.getenv("AES_PASSWORD");
        IV = System.getenv("AES_IV");
        String saltEnc = System.getenv("AES_SALT");
        if (PASSWORD == null || IV == null || saltEnc == null) {
            throw new IllegalStateException("AES environment variables not configured");
        }
        SALT = Base64.getDecoder().decode(saltEnc);
    }

    private AES256() {
        // utility class
    }

    public static String processCrypto(String inputText, int type) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(PASSWORD.toCharArray(), SALT, 1000, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            IvParameterSpec ivs = new IvParameterSpec(IV.getBytes(StandardCharsets.US_ASCII));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(type, secret, ivs);

            if (type == Cipher.ENCRYPT_MODE) {
                byte[] encrypted = cipher.doFinal(inputText.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(encrypted);
            } else if (type == Cipher.DECRYPT_MODE) {
                byte[] decodedBytes = Base64.getDecoder().decode(inputText);
                return new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("Invalid cipher mode");
            }

        } catch (Exception e) {
            LOG.error("Error during AES processing", e);
            return null;
        }
    }
}
