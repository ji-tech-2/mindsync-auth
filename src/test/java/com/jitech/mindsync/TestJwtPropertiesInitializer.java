package com.jitech.mindsync;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class TestJwtPropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "mindsync.jwt.private-key=" + privateKey,
                    "mindsync.jwt.public-key=" + publicKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate test JWT keys", e);
        }
    }
}
