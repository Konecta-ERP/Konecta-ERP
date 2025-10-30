package com.konecta.recruitmentservice.service;

import com.konecta.recruitmentservice.dto.MakeOfferDto;
import com.konecta.recruitmentservice.dto.OfferDto;
import com.konecta.recruitmentservice.entity.Applicant;
import com.konecta.recruitmentservice.entity.Offer;
import com.konecta.recruitmentservice.model.enums.ApplicantStatus;
import com.konecta.recruitmentservice.repository.ApplicantRepository;
import com.konecta.recruitmentservice.repository.OfferRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfferService {

  private final OfferRepository offerRepository;
  private final ApplicantRepository applicantRepository;

  @Autowired
  public OfferService(OfferRepository offerRepository, ApplicantRepository applicantRepository) {
    this.offerRepository = offerRepository;
    this.applicantRepository = applicantRepository;
  }

  @Transactional
  public OfferDto makeOffer(Integer applicantId, MakeOfferDto dto) {
    Applicant app = applicantRepository.findById(applicantId)
        .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));

    if (app.getOffer() != null) {
      throw new IllegalStateException("An offer has already been made to this applicant.");
    }

    Offer offer = new Offer();
    offer.setApplicant(app);
    offer.setNetSalary(dto.getNetSalary());
    offer.setGrossSalary(dto.getGrossSalary());
    offer.setBenefits(dto.getBenefits());
    offer.setStartDate(dto.getStartDate());

    Offer savedOffer = offerRepository.save(offer);

    // update applicant status
    app.setStatus(ApplicantStatus.OFFERED);
    applicantRepository.save(app);

    return convertToDto(savedOffer);
  }

  private OfferDto convertToDto(Offer offer) {
    OfferDto dto = new OfferDto();
    dto.setId(offer.getId());
    dto.setNetSalary(offer.getNetSalary());
    dto.setGrossSalary(offer.getGrossSalary());
    dto.setBenefits(offer.getBenefits());
    dto.setStartDate(offer.getStartDate());
    dto.setApplicantId(offer.getApplicant().getId());
    return dto;
  }
}