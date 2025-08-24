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

public class AES256 {

        private static final Logger LOG = LoggerFactory.getLogger(AES256.class);

	public static String processCrypto(String inputText, int type) {
		final String password = "KJH#$@kds32@!kjhdkftt";
		final String iv = "16806642kbM7c5!$";
		byte[] salt = new byte[] { 34, (byte) 134, (byte) 145, 12, 7, 6, (byte) 243, 63, 43, 54, 75, 65, 53, 2, 34, 54, 45, 67, 64, 64, 32, (byte) 213 };

		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1000, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			IvParameterSpec ivs = new IvParameterSpec(iv.getBytes(StandardCharsets.US_ASCII));

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

