package me.mamre.repo;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rule_version",
        uniqueConstraints = @UniqueConstraint(columnNames = {"decision_key","version"}))
public class RuleVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="decision_key", nullable=false) private String decisionKey;
    @Column(nullable=false) private Integer version;
    @Column(nullable=false, length=64) private String sha256;

    @Lob @Column(nullable=false) private byte[] xml;

    @Column(nullable=false) private Boolean enabled = true;
    @Column(name="uploaded_at", nullable=false) private Instant uploadedAt = Instant.now();

    public Long getId() { return id; }
    public String getDecisionKey() { return decisionKey; }
    public void setDecisionKey(String k) { this.decisionKey = k; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { this.version = v; }
    public String getSha256() { return sha256; }
    public void setSha256(String s) { this.sha256 = s; }
    public byte[] getXml() { return xml; }
    public void setXml(byte[] xml) { this.xml = xml; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}