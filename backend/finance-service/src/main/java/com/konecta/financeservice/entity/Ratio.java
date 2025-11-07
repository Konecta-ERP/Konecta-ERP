package com.konecta.financeservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "ratios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Ratio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ratio_id")
    private Long ratioId;

    @Column(name = "ratio_name", length = 20, unique = true, nullable = false)
    private String ratioName;

    @Column(name = "benchmark_value", precision = 12, scale = 2, nullable = false)
    private BigDecimal benchmarkValue = BigDecimal.ZERO;

    @Column(name = "warning_threshold", precision = 12, scale = 2, nullable = false)
    private BigDecimal warningThreshold = BigDecimal.ZERO;

}
