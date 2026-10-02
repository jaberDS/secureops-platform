package com.secureops.backend.siem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SiemExporterConfig {

    @Bean
    public RestClient splunkRestClient(@Value("${secureops.siem.url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }
}
