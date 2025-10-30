package com.Machine.demo.controller;

import com.Machine.demo.model.SignalLog;
import com.Machine.demo.model.AnalizadorTecnico.SignalDecision;
import com.Machine.demo.model.AnalizadorTecnico;
import com.Machine.demo.repository.SignalLogRepository;
import com.Machine.demo.service.GroqService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.ConcurrentHashMap;


import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tradingview")
public class TradingViewController {

    private final SignalLogRepository signalLogRepository;
    private final GroqService groqService;
    private final Map<String, SignalDecision> ultimaSeñalPorSymbol = new ConcurrentHashMap<>();

    
    public TradingViewController(SignalLogRepository signalLogRepository, GroqService groqService) {
        this.signalLogRepository = signalLogRepository;
        this.groqService = groqService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> receiveSignal(@RequestBody Map<String, Object> payload) {
        String signal = (String) payload.get("signal");
        String symbol = (String) payload.get("symbol");

        System.out.println("🔔 Señal recibida: " + symbol + " - " + signal);

        Map<String, List<Double>> preciosPorTimeframe = Map.of(
            "1h", Arrays.asList(28000.0, 28200.0, 28150.0, 28300.0, 28500.0, 28450.0, 28600.0),
            "4h", Arrays.asList(27500.0, 27800.0, 27750.0, 27900.0, 28050.0, 28100.0, 28200.0),
            "1d", Arrays.asList(26000.0, 26500.0, 26300.0, 26700.0, 27000.0, 27100.0, 27200.0)
        );

        Map<String, Double> volumenActual = Map.of("1h", 150.0, "4h", 200.0, "1d", 500.0);
        Map<String, Double> volumenPromedio = Map.of("1h", 100.0, "4h", 180.0, "1d", 450.0);

        List<SignalDecision> señales = AnalizadorTecnico.analizarMultiTimeframe(preciosPorTimeframe, volumenActual, volumenPromedio);

        SignalLog logEntry = new SignalLog();
        logEntry.setTimestamp(LocalDateTime.now());
        logEntry.setSymbol(symbol);
        logEntry.setSignal(signal);
        logEntry.setTimeframe(String.join(", ", preciosPorTimeframe.keySet()));

        boolean ejecutar = false;

        if (!señales.isEmpty()) {
            SignalDecision mejorSeñal = señales.get(0);
            String decision = "Señal técnica: " + mejorSeñal.decision + " (" + mejorSeñal.probabilidad + "%)";
            logEntry.setDecision(decision);
            ultimaSeñalPorSymbol.put(symbol, mejorSeñal);

            if (mejorSeñal.probabilidad >= 65) ejecutar = true;
            logEntry.setExecuted(ejecutar);
            signalLogRepository.save(logEntry);

            if (ejecutar) executeTrade(symbol, signal);

            return ResponseEntity.ok(Map.of(
                "status", "ok",
                "decision", decision,
                "log_id", logEntry.getId()
            ));
        }

        // Caso IA asíncrona con Groq
        groqService.askGroqAsync("Recibí una señal " + signal + " para " + symbol + ". ¿Es recomendable ejecutar esta orden?")
            .thenAccept(decision -> {
                boolean ejecutarIA = decision.toLowerCase().contains("sí") ||
                                     decision.toLowerCase().contains("long") ||
                                     decision.toLowerCase().contains("short");

                logEntry.setDecision(decision);
                logEntry.setExecuted(ejecutarIA);
                signalLogRepository.save(logEntry);
                if (ejecutarIA) executeTrade(symbol, signal);
                System.out.println("🤖 Decisión Groq final: " + decision);
            })
            .exceptionally(ex -> {
                logEntry.setDecision("ERROR IA: " + ex.getMessage());
                logEntry.setExecuted(false);
                signalLogRepository.save(logEntry);
                System.out.println("⚠ Error IA: " + ex.getMessage());
                return null;
            });

        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "decision", "Procesando IA...",
            "log_id", logEntry.getId()
        ));
    }

    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        List<SignalDecision> señales = new ArrayList<>(ultimaSeñalPorSymbol.values());
        String decisionFinal = señales.isEmpty() ? "WAIT" :
                señales.stream().max(Comparator.comparingInt(s -> s.probabilidad)).get().decision;

        return ResponseEntity.ok(Map.of(
            "señales", señales,
            "decision", decisionFinal
        ));
    }

    @GetMapping("/log")
    public ResponseEntity<List<SignalLog>> getSignalLog() {
        return ResponseEntity.ok(signalLogRepository.findAll());
    }

    private void executeTrade(String symbol, String action) {
        System.out.println("🚀 Ejecutando operación: " + action + " en " + symbol);
    }
    @GetMapping("/signals")
    public ResponseEntity<List<SignalLog>> getSignals() {
        return ResponseEntity.ok(signalLogRepository.findAll());
    }


}
