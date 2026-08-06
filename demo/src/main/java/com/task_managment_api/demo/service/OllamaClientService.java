package com.task_managment_api.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OllamaClientService {

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaApiUrl;

    @Value("${ollama.model:llama3.2}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String prompt) {
        String endpoint = ollamaApiUrl + "/api/generate";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object text = response.getBody().get("response");
                if (text != null) {
                    return text.toString().trim();
                }
            }
        } catch (Exception e) {
            log.warn("Ollama local API unavailable at {}: {}. Falling back to internal engine.", endpoint, e.getMessage());
        }
        return null;
    }
}
