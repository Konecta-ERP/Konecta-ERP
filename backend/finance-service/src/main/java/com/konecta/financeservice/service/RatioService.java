package com.konecta.financeservice.service;

import com.konecta.financeservice.dto.CreateOrUpdateRatioDTO;
import com.konecta.financeservice.dto.RatioDTO;
import com.konecta.financeservice.entity.Ratio;
import com.konecta.financeservice.repository.RatioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RatioService {

    private final RatioRepository ratioRepository;

    @Autowired
    public RatioService(RatioRepository ratioRepository) {
        this.ratioRepository = ratioRepository;
    }

    @Transactional
    public RatioDTO createRatio(CreateOrUpdateRatioDTO dto) {
        Ratio ratio = new Ratio();
        ratio.setRatioName(dto.getRatioName());
        ratio.setBenchmarkValue(dto.getBenchmarkValue());
        ratio.setWarningThreshold(dto.getWarningThreshold());
        Ratio savedRatio = ratioRepository.save(ratio);
        return convertToDTO(savedRatio);
    }

    @Transactional
    public RatioDTO updateRatio(Long id, CreateOrUpdateRatioDTO dto) {
        Ratio ratio = ratioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such ratio"));
        if (dto.getRatioName() != null) ratio.setRatioName(dto.getRatioName());
        if (dto.getBenchmarkValue() != null) ratio.setBenchmarkValue(dto.getBenchmarkValue());
        if (dto.getWarningThreshold() != null) ratio.setWarningThreshold(dto.getWarningThreshold());
        Ratio updatedRatio = ratioRepository.save(ratio);
        return convertToDTO(updatedRatio);
    }

    public List<RatioDTO> getALlRatios() {
        return ratioRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public RatioDTO getRatio(Long id) {
        Ratio ratio = ratioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such ratio"));
        return convertToDTO(ratio);
    }

    public RatioDTO convertToDTO(Ratio ratio) {
        RatioDTO dto = new RatioDTO();
        dto.setRatioId(ratio.getRatioId());
        dto.setRatioName(ratio.getRatioName());
        dto.setBenchmarkValue(ratio.getBenchmarkValue());
        dto.setWarningThreshold(ratio.getWarningThreshold());
        return dto;
    }
}
