package com.konecta.recruitmentservice.service;

import com.konecta.recruitmentservice.dto.CreateRequisitionDto;
import com.konecta.recruitmentservice.dto.JobRequisitionDto;
import com.konecta.recruitmentservice.dto.UpdateRequisitionDto;
import com.konecta.recruitmentservice.entity.JobRequisition;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import com.konecta.recruitmentservice.repository.JobRequisitionRepository;
import com.konecta.recruitmentservice.service.specification.RequisitionSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequisitionService {

  private final JobRequisitionRepository requisitionRepository;
  private final JobPostService jobPostService;

  @Autowired
  public RequisitionService(JobRequisitionRepository requisitionRepository, JobPostService jobPostService) {
    this.requisitionRepository = requisitionRepository;
    this.jobPostService = jobPostService;
  }

  @Transactional
  public JobRequisitionDto createRequisition(CreateRequisitionDto dto) {
    JobRequisition req = new JobRequisition();
    req.setReason(dto.getReason());
    req.setPriority(dto.getPriority());
    req.setOpenings(dto.getOpenings());
    req.setDepartmentId(dto.getDepartmentId());
    req.setStatus(RequisitionStatus.PENDING);
    JobRequisition savedReq = requisitionRepository.save(req);
    return convertToDto(savedReq);
  }

  @Transactional(readOnly = true)
  public JobRequisitionDto getRequisition(Integer requisitionId) {
    JobRequisition req = requisitionRepository.findById(requisitionId)
        .orElseThrow(() -> new EntityNotFoundException("JobRequisition not found with id: " + requisitionId));
    return convertToDto(req);
  }

  @Transactional(readOnly = true)
  public List<JobRequisitionDto> searchRequisitions(Integer departmentId, RequisitionStatus status) {
    Specification<JobRequisition> spec = RequisitionSpecification.findByCriteria(departmentId, status);
    return requisitionRepository.findAll(spec).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public JobRequisitionDto updateRequisition(Integer requisitionId, UpdateRequisitionDto dto) {
    JobRequisition req = requisitionRepository.findById(requisitionId)
        .orElseThrow(() -> new EntityNotFoundException("JobRequisition not found with id: " + requisitionId));

    // Apply updates only if they are present in the DTO
    dto.getReason().ifPresent(req::setReason);
    dto.getPriority().ifPresent(req::setPriority);
    dto.getOpenings().ifPresent(req::setOpenings);
    dto.getStatus().ifPresent(req::setStatus);

    JobRequisition savedReq = requisitionRepository.save(req);
    return convertToDto(savedReq);
  }

  private JobRequisitionDto convertToDto(JobRequisition req) {
    JobRequisitionDto dto = new JobRequisitionDto();
    dto.setId(req.getId());
    dto.setReason(req.getReason());
    dto.setPriority(req.getPriority());
    dto.setOpenings(req.getOpenings());
    dto.setStatus(req.getStatus());
    dto.setCreatedAt(req.getCreatedAt());
    dto.setDepartmentId(req.getDepartmentId());

    if (req.getJobPosts() != null) {
      dto.setJobPosts(
          req.getJobPosts().stream()
              .map(jobPost -> jobPostService.convertToDto(jobPost))
              .collect(Collectors.toList()));
    }
    return dto;
  }
}