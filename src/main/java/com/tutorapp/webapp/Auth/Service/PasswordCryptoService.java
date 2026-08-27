package com.tutorapp.webapp.Auth.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Auth.Entity.PasswordKey;
import com.tutorapp.webapp.Auth.Repository.PasswordKeyRepository;

@Service
public class PasswordCryptoService {

    private final KeyPair keyPair;
    private final String keyId;
    private final PasswordKeyRepository passwordKeyRepository;

    public PasswordCryptoService(PasswordKeyRepository passwordKeyRepository) throws Exception {
        this.passwordKeyRepository = passwordKeyRepository;

        Optional<PasswordKey> existingKey = passwordKeyRepository.findById("current");
        if (existingKey.isPresent()) {
            PasswordKey persisted = existingKey.get();
            this.keyId = persisted.getKeyId();
            this.keyPair = reconstructKeyPair(persisted.getPublicKey(), persisted.getPrivateKey());
        } else {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            this.keyPair = generator.generateKeyPair();
            this.keyId = UUID.randomUUID().toString();
            saveKeyPair();
        }
    }

    private KeyPair reconstructKeyPair(String publicKeyBase64, String privateKeyBase64) throws Exception {
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64);
        byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64);

        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(publicSpec);

        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
        RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(privateSpec);

        return new KeyPair(publicKey, privateKey);
    }

    private void saveKeyPair() {
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        PasswordKey passwordKey = new PasswordKey(keyId, publicKeyBase64, privateKeyBase64);
        passwordKeyRepository.save(passwordKey);
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
