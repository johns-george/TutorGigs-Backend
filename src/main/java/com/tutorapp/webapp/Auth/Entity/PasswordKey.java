package com.tutorapp.webapp.Auth.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_key")
public class PasswordKey {

    @Id
    @Column(name = "id")
    private String id = "current";

    @Column(name = "key_id", nullable = false)
    private String keyId;

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "private_key", nullable = false, columnDefinition = "TEXT")
    private String privateKey;

    public PasswordKey() {}

    public PasswordKey(String keyId, String publicKey, String privateKey) {
        this.id = "current";
        this.keyId = keyId;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
}
