package com.konecta.recruitmentservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.konecta.recruitmentservice.dto.MakeOfferDto;
import com.konecta.recruitmentservice.dto.OfferDto;
import com.konecta.recruitmentservice.dto.response.ApiResponse;
import com.konecta.recruitmentservice.service.OfferService;

import jakarta.validation.Valid;

@RestController
public class OfferController {

  private final OfferService offerService;

  @Autowired
  public OfferController(OfferService offerService) {
    this.offerService = offerService;
  }

  @PostMapping("/applicants/{applicantId}/offer")
  public ResponseEntity<ApiResponse<OfferDto>> makeOffer(
      @PathVariable Integer applicantId,
      @Valid @RequestBody MakeOfferDto dto) {

    OfferDto newOffer = offerService.makeOffer(applicantId, dto);
    ApiResponse<OfferDto> response = ApiResponse.success(
        newOffer,
        HttpStatus.CREATED.value(),
        "Offer made successfully.",
        "Offer created with id " + newOffer.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}