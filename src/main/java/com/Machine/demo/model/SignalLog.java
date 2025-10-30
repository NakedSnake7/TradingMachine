package com.Machine.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signal_log")
public class SignalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "signal_timestamp")
    private LocalDateTime signalTimestamp;
    private String symbol;

    @Column(name = "signal_name") // ✅ evita conflicto
    private String signalName;

    private String timeframe;
    private String decision;
    private boolean executed;

    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return signalTimestamp; }
    public String getSymbol() { return symbol; }
    public String getSignal() { return signalName; }
    public String getTimeframe() { return timeframe; }
    public String getDecision() { return decision; }
    public boolean isExecuted() { return executed; }

    public void setId(Long id) { this.id = id; }
    public void setTimestamp(LocalDateTime timestamp) { this.signalTimestamp = timestamp; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setSignal(String signal) { this.signalName = signal; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public void setDecision(String decision) { this.decision = decision; }
    public void setExecuted(boolean executed) { this.executed = executed; }
}
