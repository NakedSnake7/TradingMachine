package com.Machine.demo.model;

import java.util.*;

public class AnalizadorTecnico {

    public static class SignalDecision {
        public String decision;       // LONG / SHORT / WAIT
        public int probabilidad;      // % de confianza
        public String timeframe;
        public double stopLoss;
        public double takeProfit;

        public SignalDecision(String decision, int probabilidad, String timeframe, double stopLoss, double takeProfit) {
            this.decision = decision;
            this.probabilidad = probabilidad;
            this.timeframe = timeframe;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
        }

        @Override
        public String toString() {
            return decision + " (" + probabilidad + "%) [" + timeframe + "] SL: " + stopLoss + " TP: " + takeProfit;
        }
    }

    // Analiza multi-timeframe
    public static List<SignalDecision> analizarMultiTimeframe(
            Map<String, List<Double>> preciosPorTimeframe,
            Map<String, Double> volumenActual,
            Map<String, Double> volumenPromedio) {

        List<SignalDecision> señales = new ArrayList<>();

        for (String tf : preciosPorTimeframe.keySet()) {
            List<Double> precios = preciosPorTimeframe.get(tf);
            double rsi = calcularRSI(precios);
            double ema12 = calcularEMA(precios, 12);
            double ema26 = calcularEMA(precios, 26);
            double macd = ema12 - ema26;
            double macdHist = calcularMACDHistograma(precios);
            double volRatio = volumenActual.getOrDefault(tf, 0.0) / Math.max(1.0, volumenPromedio.getOrDefault(tf, 1.0));

            String decision = generarDecisionAvanzada(rsi, ema12, ema26, macd, macdHist, volRatio, precios);
            int probabilidad = calcularProbabilidadAvanzada(rsi, macd, macdHist, volRatio);

            double[] slTp = calcularSLTP(precios.get(precios.size() - 1), decision);
            señales.add(new SignalDecision(decision, probabilidad, tf, slTp[0], slTp[1]));
        }

        señales.sort((a, b) -> b.probabilidad - a.probabilidad);
        return señales;
    }

    // RSI
    public static double calcularRSI(List<Double> precios) {
        int periodo = 14;
        if (precios.size() < periodo + 1) return 50.0;

        double ganancia = 0.0;
        double perdida = 0.0;

        for (int i = precios.size() - periodo; i < precios.size(); i++) {
            double diff = precios.get(i) - precios.get(i - 1);
            if (diff > 0) ganancia += diff;
            else perdida -= diff;
        }

        if (ganancia + perdida == 0) return 50.0;
        double rs = ganancia / perdida;
        return 100 - (100 / (1 + rs));
    }

    // EMA
    public static double calcularEMA(List<Double> precios, int periodo) {
        if (precios.isEmpty()) return 0.0;
        double k = 2.0 / (periodo + 1);
        double ema = precios.get(0);

        for (int i = 1; i < precios.size(); i++) {
            ema = precios.get(i) * k + ema * (1 - k) * ema;
        }
        return ema;
    }

    // MACD Histograma simplificado
    public static double calcularMACDHistograma(List<Double> precios) {
        double ema12 = calcularEMA(precios, 12);
        double ema26 = calcularEMA(precios, 26);
        return ema12 - ema26; // Histograma básico: MACD - Signal (signal=EMA9)
    }

    // Generar decisión avanzada con EMA cruzadas, RSI y MACD
    private static String generarDecisionAvanzada(double rsi, double ema12, double ema26, double macd, double macdHist, double volRatio, List<Double> precios) {
        boolean emaCruzadaAlcista = ema12 > ema26;
        boolean emaCruzadaBajista = ema12 < ema26;

        boolean rsiSobreventa = rsi < 30;
        boolean rsiSobrecompra = rsi > 70;

        boolean macdAlcista = macdHist > 0;
        boolean macdBajista = macdHist < 0;

        // LONG
        if (emaCruzadaAlcista && rsiSobreventa && macdAlcista && volRatio > 1.0) return "LONG";

        // SHORT
        if (emaCruzadaBajista && rsiSobrecompra && macdBajista && volRatio > 1.0) return "SHORT";

        return "WAIT";
    }

    // Calcular probabilidad avanzada
    private static int calcularProbabilidadAvanzada(double rsi, double macd, double macdHist, double volRatio) {
        int prob = 50;

        if (rsi < 20 || rsi > 80) prob += 20;
        else if (rsi < 30 || rsi > 70) prob += 10;

        if (Math.abs(macdHist) > 50) prob += 15;
        else if (Math.abs(macdHist) > 20) prob += 10;

        if (volRatio > 1.5) prob += 15;
        else if (volRatio > 1.2) prob += 10;

        return Math.min(prob, 100);
    }

    // Calcular Stop-Loss y Take-Profit simple
    private static double[] calcularSLTP(double precioActual, String decision) {
        double sl = precioActual;
        double tp = precioActual;

        if (decision.equals("LONG")) {
            sl = precioActual * 0.98; // 2% por debajo
            tp = precioActual * 1.04; // 4% arriba
        } else if (decision.equals("SHORT")) {
            sl = precioActual * 1.02; // 2% arriba
            tp = precioActual * 0.96; // 4% abajo
        }
        return new double[]{sl, tp};
    }
}
