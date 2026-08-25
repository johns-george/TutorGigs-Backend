package com.tutorapp.webapp.Auth.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.springframework.stereotype.Service;

@Service
public class PasswordCryptoService {

    private final KeyPair keyPair;
    private final String keyId;

    public PasswordCryptoService() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        this.keyPair = generator.generateKeyPair();
        this.keyId = UUID.randomUUID().toString();
    }

    public Map<String, String> getPublicKeyPayload() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return Map.of(
                "keyId", keyId,
                "algorithm", "RSA-OAEP-256",
                "publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    }

    public String decryptPassword(String encryptedPassword, String passwordKeyId) {
        if (encryptedPassword == null || encryptedPassword.isBlank()) {
            throw new IllegalArgumentException("Encrypted password is required");
        }
        if (passwordKeyId == null || !keyId.equals(passwordKeyId)) {
            throw new IllegalArgumentException("Password encryption key is expired");
        }

        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), oaepParams);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to decrypt password", e);
        }
    }
}
