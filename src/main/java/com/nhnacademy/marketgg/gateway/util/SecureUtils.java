package com.nhnacademy.marketgg.gateway.util;

import com.nhnacademy.marketgg.gateway.exception.ClientHttpConnectionException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;

/**
 * 보안 인증서 처리와 관련된 유틸리티 클래스입니다.
 */
@Slf4j
@Component
public class SecureUtils {

    @Value("${gg.keystore.type}")
    private String keystoreType;

    @Value("${gg.keystore.path}")
    private String keystorePath;

    @Value("${gg.keystore.password}")
    private String keystorePassword;

    /**
     * ClientHttpConnector 를 얻어옵니다.
     *
     * @return ClientHttpConnector
     */
    // @Bean
    public ClientHttpConnector getClientHttpConnector() {
        log.info("Market GG key store type: {}", keystoreType);
        log.info("Market GG key store path: {}", keystorePath);
        log.info("Market GG key store password: {}", keystorePassword);

        try {
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            Resource resource = new ClassPathResource(keystorePath);
            keyStore.load(resource.getInputStream(), keystorePassword.toCharArray());

            KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            KeyStore trustStore = KeyStore.getInstance(keystoreType);
            Resource trustResource = new ClassPathResource(keystorePath);
            trustStore.load(trustResource.getInputStream(), keystorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            SslContext sslContext = SslContextBuilder.forClient()
                                                     .keyManager(keyManagerFactory)
                                                     // .trustManager(trustManagerFactory)
                                                     .build();

            HttpClient httpClient = HttpClient.create()
                                              .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

            return new ReactorClientHttpConnector(httpClient);

        } catch (Exception ex) {
            log.error("An error has occurred: ", ex);
            throw new ClientHttpConnectionException(ex);
        }
    }

}
