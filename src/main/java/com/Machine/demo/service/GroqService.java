package com.Machine.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GroqService {

    private final String groqApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();

    private static final String GROQ_URL = "https://api.groq.com/v1/chat/completions";
    private static final long CACHE_EXPIRATION_MS = 5 * 60 * 1000; // 5 minutos

    public GroqService(@Value("${groq.api.key}") String groqApiKey) {
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            throw new IllegalStateException("ERROR: La propiedad groq.api.key no está definida en application.properties");
        }
        this.groqApiKey = groqApiKey;
    }

    // Clase interna para cache con expiración
    private static class CachedResponse {
        String content;
        long timestamp;

        CachedResponse(String content) {
            this.content = content;
            this.timestamp = Instant.now().toEpochMilli();
        }
    }

    public CompletableFuture<String> askGroqAsync(String prompt) {
        CachedResponse cached = cache.get(prompt);
        if (cached != null && Instant.now().toEpochMilli() - cached.timestamp < CACHE_EXPIRATION_MS) {
            return CompletableFuture.completedFuture(cached.content);
        }

        return CompletableFuture.supplyAsync(() -> askGroqWithRetries(prompt, 3));
    }

    @SuppressWarnings("unchecked")
    private String askGroqWithRetries(String prompt, int retries) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> payload = Map.of(
                "model", "llama3-70b-8192",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.6,
                "max_tokens", 200
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        for (int i = 0; i < retries; i++) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(GROQ_URL, request, (Class<Map<String, Object>>)(Class<?>) Map.class);

                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");

                // Guardar en cache
                cache.put(prompt, new CachedResponse(content));
                return content;

            } catch (Exception e) {
                System.out.println("⚠ Groq error (intento " + (i+1) + "): " + e.getMessage());
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        System.out.println("⚠ Groq falló, usando respuesta simulada.");
        return simulateIA(prompt);
    }

    private String simulateIA(String prompt) {
        return new Random().nextDouble() < 0.7 ? "Sí, ejecutar la orden." : "No, esperar antes de operar.";
    }
}
