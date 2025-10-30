package com.konecta.recruitmentservice.service;

import com.konecta.recruitmentservice.dto.CreateJobPostDto;
import com.konecta.recruitmentservice.dto.JobPostDto;
import com.konecta.recruitmentservice.entity.JobPost;
import com.konecta.recruitmentservice.entity.JobRequisition;
import com.konecta.recruitmentservice.model.enums.RequisitionStatus;
import com.konecta.recruitmentservice.repository.JobPostRepository;
import com.konecta.recruitmentservice.repository.JobRequisitionRepository;
import com.konecta.recruitmentservice.service.specification.JobPostSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostService {

  private final JobPostRepository postRepository;
  private final JobRequisitionRepository requisitionRepository;

  @Autowired
  public JobPostService(JobPostRepository postRepository, JobRequisitionRepository requisitionRepository) {
    this.postRepository = postRepository;
    this.requisitionRepository = requisitionRepository;
  }

  @Transactional
  public JobPostDto createJobPost(CreateJobPostDto dto) {
    JobRequisition req = requisitionRepository.findById(dto.getRequisitionId())
        .orElseThrow(() -> new EntityNotFoundException("JobRequisition not found with id: " + dto.getRequisitionId()));

    if (req.getStatus() != RequisitionStatus.APPROVED) {
      throw new IllegalStateException("Cannot create a job post for a requisition that is not APPROVED.");
    }

    JobPost post = new JobPost();
    post.setJobRequisition(req);
    post.setTitle(dto.getTitle());
    post.setDescription(dto.getDescription());
    post.setRequirements(dto.getRequirements());
    post.setActive(false);

    JobPost savedPost = postRepository.save(post);
    return convertToDto(savedPost);
  }

  @Transactional(readOnly = true)
  public JobPostDto getJobPost(Integer postId) {
    JobPost post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("JobPost not found with id: " + postId));
    return convertToDto(post);
  }

  @Transactional(readOnly = true)
  public List<JobPostDto> searchJobPosts(String position, Integer departmentId, Boolean active) {
    Specification<JobPost> spec = JobPostSpecification.findByCriteria(position, departmentId, active);
    return postRepository.findAll(spec).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public JobPostDto activateJobPost(Integer postId, boolean active) {
    JobPost post = postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException("JobPost not found with id: " + postId));

    post.setActive(active);
    JobPost savedPost = postRepository.save(post);
    return convertToDto(savedPost);
  }

  JobPostDto convertToDto(JobPost post) {
    JobPostDto dto = new JobPostDto();
    dto.setId(post.getId());
    dto.setTitle(post.getTitle());
    dto.setDescription(post.getDescription());
    dto.setRequirements(post.getRequirements());
    dto.setActive(post.isActive());
    dto.setPostedAt(post.getPostedAt());
    dto.setRequisitionId(post.getJobRequisition().getId());
    return dto;
  }
}