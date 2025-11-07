package com.konecta.employeeservice.repository;

import com.konecta.employeeservice.entity.LeaveRequest;
import com.konecta.employeeservice.model.enums.RequestStatus;
import com.konecta.employeeservice.model.enums.RequestType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {

  List<LeaveRequest> findByEmployeeId(Integer employeeId);

  List<LeaveRequest> findByEmployeeIdAndStatusAndRequestType(
      Integer employeeId,
      RequestStatus status,
      RequestType requestType);
}
