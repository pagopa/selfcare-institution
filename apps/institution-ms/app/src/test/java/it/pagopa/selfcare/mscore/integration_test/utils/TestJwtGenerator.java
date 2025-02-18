package it.pagopa.selfcare.mscore.integration_test.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import it.pagopa.selfcare.mscore.integration_test.model.JwtData;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class TestJwtGenerator {

    private final RSAPrivateKey privateKey;

    public TestJwtGenerator() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.privateKey = readPrivateKey();
    }

    public String generateToken(JwtData jwtData) {
        return Optional.ofNullable(jwtData).map(jd ->
                JWT.create()
                .withHeader(jwtData.getJwtHeader())
                .withPayload(jwtData.getJwtPayload())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(3600))
                .sign(Algorithm.RSA256(privateKey))
        ).orElse(null);
    }

    private RSAPrivateKey readPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        log.info("Reading private key");
        final File file = new File("src/test/resources/key/private-key.pem");
        String privateKeyString = new String(Files.readAllBytes(file.toPath()));
        privateKeyString = privateKeyString.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s+","");
        final byte [] pKeyEncoded = Base64.decode(privateKeyString);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pKeyEncoded);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

}
