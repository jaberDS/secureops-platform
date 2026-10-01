package com.secureops.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String detectionRule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ThreatSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_event_id")
    private SecurityEvent sourceEvent;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DetectionStatus status;

    @Column(nullable = false)
    private Integer riskScore;

    public SecurityAlert() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetectionRule() {
        return detectionRule;
    }

    public void setDetectionRule(String detectionRule) {
        this.detectionRule = detectionRule;
    }

    public ThreatSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ThreatSeverity severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SecurityEvent getSourceEvent() {
        return sourceEvent;
    }

    public void setSourceEvent(SecurityEvent sourceEvent) {
        this.sourceEvent = sourceEvent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DetectionStatus getStatus() {
        return status;
    }

    public void setStatus(DetectionStatus status) {
        this.status = status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
}
