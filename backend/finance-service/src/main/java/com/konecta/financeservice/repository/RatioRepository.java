package com.konecta.financeservice.repository;

import com.konecta.financeservice.entity.Ratio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatioRepository extends JpaRepository<Ratio, Long> {
    Optional<Ratio> findByRatioName(String ratioName);
}
