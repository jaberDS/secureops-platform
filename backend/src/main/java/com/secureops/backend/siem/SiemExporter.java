package com.secureops.backend.siem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SiemExporter {

    private static final Logger log =
            LoggerFactory.getLogger(SiemExporter.class);

    private final RestClient client;
    private final String token;
    private final String index;
    private final String sourcetype;

    public SiemExporter(
            RestClient splunkRestClient,
            @Value("${secureops.siem.token}") String token,
            @Value("${secureops.siem.index}") String index,
            @Value("${secureops.siem.sourcetype}") String sourcetype) {

        this.client = splunkRestClient;
        this.token = token;
        this.index = index;
        this.sourcetype = sourcetype;
    }

    @Async
    public void export(SiemEvent event) {

        try {

            Map<String, Object> payload = Map.of(
                    "event", event,
                    "sourcetype", sourcetype,
                    "index", index
            );

            client.post()
                    .uri("/services/collector/event")
                    .header(
                            "Authorization",
                            "Splunk " + token
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "SIEM exported: id={} type={} severity={}",
                    event.eventId(),
                    event.eventType(),
                    event.severity()
            );

        } catch (Exception e) {

            log.warn(
                    "SIEM export failed for id={}: {}",
                    event.eventId(),
                    e.getMessage()
            );
        }
    }
}
