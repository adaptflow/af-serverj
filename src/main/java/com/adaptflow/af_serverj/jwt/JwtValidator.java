package com.adaptflow.af_serverj.jwt;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.bouncycastle.asn1.ASN1Primitive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adaptflow.af_serverj.common.exception.ErrorCode;
import com.adaptflow.af_serverj.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;

@Component
public class JwtValidator {
    public static final String SHA_512_WITH_RSA_ALGORITHM = "SHA512withRSA";
    public static final String BLACKLISTED_TOKENS_KEY = "blacklisted.jwts";
    private final String privateKeyPEM;
    private final String publicKeyPEM;
    protected final PublicKey publicKey;
    protected final PrivateKey privateKey;
    private RedissonClient redissonClient;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    public JwtValidator(@Value("${jwt.public.key}") String publicKeyPEM,
            @Value("${jwt.private.key}") String privateKeyPEM, RedissonClient redissonClient) {
        this.publicKeyPEM = publicKeyPEM;
        this.privateKeyPEM = privateKeyPEM;
        this.redissonClient = redissonClient;
        this.privateKey = loadPrivateKey(privateKeyPEM);
        this.publicKey = loadPublicKey(publicKeyPEM);
    }

    private PrivateKey loadPrivateKey(String privateKeyPem) {
        try {
            // Remove header, footer, and newlines from PEM
            String privateKeyContent = privateKeyPem
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] privateKeyDER = Base64.getDecoder().decode(privateKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            try {
                // Try PKCS#8 first
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyDER);
                return keyFactory.generatePrivate(keySpec);
            } catch (Exception e) {
                // If PKCS#8 fails, try parsing as PKCS#1
                ASN1Primitive asn1Object = ASN1Primitive.fromByteArray(privateKeyDER);
                RSAPrivateKey rsa = RSAPrivateKey.getInstance(asn1Object);
                RSAPrivateCrtKeySpec rsaSpec = new RSAPrivateCrtKeySpec(
                        rsa.getModulus(), rsa.getPublicExponent(), rsa.getPrivateExponent(),
                        rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(),
                        rsa.getExponent2(), rsa.getCoefficient());
                return keyFactory.generatePrivate(rsaSpec);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key :", e);
        }
    }

    private PublicKey loadPublicKey(String publicKeyPEM) {
        try {
            // Remove header, footer, and newlines from PEM
            String publicKeyContent = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Decode the public key
            byte[] publicKeyDER = Base64.getDecoder().decode(publicKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyDER);

            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key :", e);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean validateToken(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "Invalid token structure.");
        }

        // checking if the token is blacklisted
        RBucket<List<String>> blacklistedTokensList = redissonClient.getBucket(BLACKLISTED_TOKENS_KEY);
        if (blacklistedTokensList.get() != null && blacklistedTokensList.get().contains(token)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Token is blacklisted.");
        }

        // Decode header & payload
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        Map<String, Object> header = objectMapper.readValue(headerJson, Map.class);
        Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

        // Check token expiration
        if (payload.containsKey("exp")) {
            long expiration = Long.parseLong(payload.get("exp").toString());
            if (Instant.now().getEpochSecond() > expiration) {
                throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Token expired.");
            }
        } else {
            throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Token does not contain expiration.");
        }

        // Verify signature
        String signedContent = parts[0] + "." + parts[1];
        byte[] signature = Base64.getUrlDecoder().decode(parts[2]);

        java.security.Signature sig = java.security.Signature.getInstance(SHA_512_WITH_RSA_ALGORITHM);
        sig.initVerify(publicKey);
        sig.update(signedContent.getBytes(StandardCharsets.UTF_8));

        if (!sig.verify(signature)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED_ACCESS, "Invalid token signature.");
        }

        return true;
    }
}
