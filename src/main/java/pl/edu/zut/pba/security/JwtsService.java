package pl.edu.zut.pba.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springdoc.core.parsers.ReturnTypeParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtsService
{

    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final ObjectMapper objectMapper;
    private final ReturnTypeParser returnTypeParser;

    public JwtsService(
            @Value("classpath:lab06/private_key.pem") Resource privateKey,
            @Value("classpath:lab06/public_key.pem") Resource publicKey,
            ObjectMapper objectMapper,
            ReturnTypeParser returnTypeParser) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        this.objectMapper = objectMapper;

        byte[] publicKeyBytes = publicKey.getContentAsByteArray();
        String publicKeyContent = new String(publicKeyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        KeyFactory publicKeyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = publicKeyFactory.generatePublic(publicKeySpec);

        // Load Private Key
        byte[] privateKeyBytes = privateKey.getContentAsByteArray();
        String privateKeyContent = new String(privateKeyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = privateKeyFactory.generatePrivate(privateKeySpec);
        this.returnTypeParser = returnTypeParser;
    }

    public String generateSignature(String data)
    {
        return Jwts.builder()
                .content(data)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean isValidSignature(String data, String signature)
    {
        try
        {
            // Verify with public key
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(signature);

            String signedPayload = objectMapper.writeValueAsString(claimsJws.getPayload());
            String payload = objectMapper.readTree(data).toString();

            return payload.equals(signedPayload);
        }
        catch (SignatureException | JsonProcessingException e)
        {
            return false;
        }
    }

}
