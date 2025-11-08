package com.konecta.employeeservice.service;

import com.konecta.employeeservice.dto.PayrollDetailDto;
import com.konecta.employeeservice.dto.PayrollSummaryDto;
import com.konecta.employeeservice.entity.AttendanceRecord;
import com.konecta.employeeservice.entity.LeaveRequest;
import com.konecta.employeeservice.entity.PayrollRecord;
import com.konecta.employeeservice.entity.PayrollRecordDetail;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.model.enums.RequestStatus;
import com.konecta.employeeservice.model.enums.RequestType;
import com.konecta.employeeservice.repository.AttendanceRecordRepository;
import com.konecta.employeeservice.repository.LeaveRequestRepository;
import com.konecta.employeeservice.repository.PayrollRecordRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollService {

  private final EmployeeRepository employeeRepository;
  private final AttendanceRecordRepository attendanceRepository;
  private final LeaveRequestRepository leaveRepository;
  private final PayrollRecordRepository payrollRecordRepository;

  // Assumption: work week is Sunday-Thursday; weekend = Friday, Saturday.
  private static final Set<java.time.DayOfWeek> WEEKEND = Set.of(java.time.DayOfWeek.FRIDAY,
      java.time.DayOfWeek.SATURDAY);

  @Autowired
  public PayrollService(
      EmployeeRepository employeeRepository,
      AttendanceRecordRepository attendanceRepository,
      LeaveRequestRepository leaveRepository,
      PayrollRecordRepository payrollRecordRepository) {
    this.employeeRepository = employeeRepository;
    this.attendanceRepository = attendanceRepository;
    this.leaveRepository = leaveRepository;
    this.payrollRecordRepository = payrollRecordRepository;
  }

  /**
   * Calculate payroll for an employee for the given year-month (YYYY-MM).
   * Assumptions:
   * - Employee.salaryGross is yearly salary (if null, use salaryNet instead)
   * - Workdays per year = 260 (52 weeks * 5 days)
   * - Workday hours = 8
   * - Overtime paid at 1.5x hourly rate
   * - Approved leaves of type VACATION or SICK are paid; UNPAID are deducted
   * - Missing clock-in for a workday is treated as unauthorized absence ->
   * full-day deduction
   */
  @Transactional
  public PayrollSummaryDto calculateAndStore(Integer employeeId, String yearMonthStr) {
    Employee emp = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

    YearMonth ym = YearMonth.parse(yearMonthStr);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    // If the requested month includes future dates (e.g., caller asked this month
    // before it finished), cap the effective end to today to avoid calculating
    // future working days.
    LocalDate today = LocalDate.now();
    LocalDate effectiveEnd = end.isAfter(today) ? today : end;

    // Determine salary basis
    BigDecimal yearlySalary = emp.getSalaryGross() != null ? emp.getSalaryGross() : emp.getSalaryNet();
    if (yearlySalary == null) {
      yearlySalary = BigDecimal.ZERO;
    }

    BigDecimal workdaysPerYear = BigDecimal.valueOf(260);
    BigDecimal dailyRate = yearlySalary.divide(workdaysPerYear, 10, RoundingMode.HALF_UP);
    BigDecimal hourlyRate = dailyRate.divide(BigDecimal.valueOf(8), 10, RoundingMode.HALF_UP);

    // Fetch attendance records and leaves in period
    // Only fetch attendance and leaves up to the effective end date (no future
    // days)
    List<AttendanceRecord> attendance = attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start,
        effectiveEnd);
    List<LeaveRequest> leaves = leaveRepository.findByEmployeeId(employeeId).stream()
        .filter(l -> l.getStatus() == RequestStatus.APPROVED)
        .filter(l -> !(l.getEndDate().isBefore(start) || l.getStartDate().isAfter(effectiveEnd)))
        .collect(Collectors.toList());

    // Precompute unpaid leave dates (cap to effectiveEnd so future dates are not
    // included)
    Set<LocalDate> unpaidLeaveDates = leaves.stream()
        .filter(l -> l.getRequestType() == RequestType.UNPAID)
        .flatMap(l -> {
          LocalDate s = l.getStartDate().isBefore(start) ? start : l.getStartDate();
          LocalDate e = l.getEndDate().isAfter(effectiveEnd) ? effectiveEnd : l.getEndDate();
          List<LocalDate> dates = new ArrayList<>();
          for (LocalDate cur = s; !cur.isAfter(e); cur = cur.plusDays(1)) {
            dates.add(cur);
          }
          return dates.stream();
        }).collect(Collectors.toSet());

    // Precompute paid leave dates (VACATION or SICK) (cap to effectiveEnd)
    Set<LocalDate> paidLeaveDates = leaves.stream()
        .filter(l -> l.getRequestType() == RequestType.VACATION || l.getRequestType() == RequestType.SICK)
        .flatMap(l -> {
          LocalDate s = l.getStartDate().isBefore(start) ? start : l.getStartDate();
          LocalDate e = l.getEndDate().isAfter(effectiveEnd) ? effectiveEnd : l.getEndDate();
          List<LocalDate> dates = new ArrayList<>();
          for (LocalDate cur = s; !cur.isAfter(e); cur = cur.plusDays(1)) {
            dates.add(cur);
          }
          return dates.stream();
        }).collect(Collectors.toSet());

    // Build map of date -> worked hours
    Map<LocalDate, BigDecimal> workedHoursByDate = new HashMap<>();
    for (AttendanceRecord ar : attendance) {
      if (ar.getClockInTime() != null && ar.getClockOutTime() != null) {
        long seconds = Duration.between(ar.getClockInTime(), ar.getClockOutTime()).getSeconds();
        BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 4, RoundingMode.HALF_UP);
        workedHoursByDate.merge(ar.getDate(), hours, BigDecimal::add);
      }
    }

    // Iterate through each workday in the month (Sun - Thu) and compute
    BigDecimal totalBasic = BigDecimal.ZERO;
    BigDecimal totalOvertime = BigDecimal.ZERO;
    BigDecimal totalDeductions = BigDecimal.ZERO;
    List<PayrollDetailDto> details = new ArrayList<>();

    for (LocalDate d = start; !d.isAfter(effectiveEnd); d = d.plusDays(1)) {
      final LocalDate day = d;
      DayOfWeek dow = day.getDayOfWeek();

      // Lookup worked hours for the day up-front
      BigDecimal hours = workedHoursByDate.getOrDefault(day, BigDecimal.ZERO);

      // If this is a weekend: do not treat as absence. If worked, count all
      // hours as overtime (weekend pay)
      if (WEEKEND.contains(dow)) {
        if (hours.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal weekendOvertime = hourlyRate.multiply(hours).multiply(BigDecimal.valueOf(1.5));
          totalOvertime = totalOvertime.add(weekendOvertime);
          details.add(createDetail("OVERTIME", "Weekend work: " + day + " (hrs=" + hours + ")", weekendOvertime));
        }
        // skip other weekend processing
        continue;
      }

      // If this day is an approved unpaid leave, apply deduction and skip
      if (unpaidLeaveDates.contains(day)) {
        totalDeductions = totalDeductions.add(dailyRate);
        details.add(createDetail("DEDUCTION", "Unpaid leave: " + day, dailyRate.negate()));
        continue;
      }

      // If there is a paid leave (VACATION or SICK) on this day, treat as paid
      if (paidLeaveDates.contains(day)) {
        totalBasic = totalBasic.add(dailyRate);
        details.add(createDetail("BASIC", "Paid leave: " + day, dailyRate));
        continue;
      }

      // Check if worked (we already read `hours` above for weekend handling)
      if (hours.compareTo(BigDecimal.ZERO) == 0) {
        // Missing clock-in -> full day deduction
        totalDeductions = totalDeductions.add(dailyRate);
        details.add(createDetail("DEDUCTION", "Unauthorized absence: " + day, dailyRate.negate()));
        continue;
      }

      // Employee worked some hours
      BigDecimal workdayHours = BigDecimal.valueOf(8);
      BigDecimal regularHours = hours.min(workdayHours);
      BigDecimal overtimeHours = hours.compareTo(workdayHours) > 0 ? hours.subtract(workdayHours) : BigDecimal.ZERO;

      BigDecimal basicForDay = hourlyRate.multiply(regularHours);
      BigDecimal overtimeForDay = hourlyRate.multiply(overtimeHours).multiply(BigDecimal.valueOf(1.5));

      totalBasic = totalBasic.add(basicForDay);
      totalOvertime = totalOvertime.add(overtimeForDay);

      details.add(createDetail("BASIC", "Work: " + day + " (hrs=" + hours + ")", basicForDay));
      if (overtimeHours.compareTo(BigDecimal.ZERO) > 0) {
        details.add(createDetail("OVERTIME", "Overtime: " + day + " (hrs=" + overtimeHours + ")", overtimeForDay));
      }

      // half-day deduction if worked less than 4 hours
      if (hours.compareTo(BigDecimal.valueOf(4)) < 0) {
        BigDecimal halfDay = dailyRate.divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);
        totalDeductions = totalDeductions.add(halfDay);
        details.add(createDetail("DEDUCTION", "Half-day deduction: " + day, halfDay.negate()));
      }
    }

    BigDecimal net = totalBasic.add(totalOvertime).subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);

    // Persist PayrollRecord and details
    PayrollRecord record = new PayrollRecord();
    record.setEmployeeId(employeeId);
    record.setYearMonth(yearMonthStr);
    record.setPeriodStart(start);
    record.setPeriodEnd(end);
    record.setBasicPay(totalBasic.setScale(2, RoundingMode.HALF_UP));
    record.setOvertimePay(totalOvertime.setScale(2, RoundingMode.HALF_UP));
    record.setDeductions(totalDeductions.setScale(2, RoundingMode.HALF_UP));
    record.setNetPay(net);

    PayrollRecord saved = payrollRecordRepository.save(record);

    Set<PayrollRecordDetail> persistedDetails = details.stream().map(d -> {
      PayrollRecordDetail pd = new PayrollRecordDetail();
      pd.setPayrollRecord(saved);
      pd.setType(d.getType());
      pd.setDescription(d.getDescription());
      pd.setAmount(d.getAmount());
      return pd;
    }).collect(Collectors.toSet());

    saved.setDetails(persistedDetails);
    payrollRecordRepository.save(saved);

    return buildSummaryFromRecord(saved);
  }

  @Transactional
  public PayrollSummaryDto retrieveOrCalculate(Integer employeeId, String yearMonth) {
    Optional<PayrollRecord> existing = payrollRecordRepository.findByEmployeeIdAndYearMonth(employeeId, yearMonth);
    if (existing.isPresent()) {
      return buildSummaryFromRecord(existing.get());
    }

    return calculateAndStore(employeeId, yearMonth);
  }

  private PayrollSummaryDto buildSummaryFromRecord(PayrollRecord r) {
    PayrollSummaryDto summary = new PayrollSummaryDto();
    summary.setEmployeeId(r.getEmployeeId());
    summary.setYearMonth(r.getYearMonth());
    summary.setBasicPay(r.getBasicPay());
    summary.setOvertimePay(r.getOvertimePay());
    summary.setDeductions(r.getDeductions());
    summary.setNetPay(r.getNetPay());
    List<PayrollDetailDto> outDetails = r.getDetails() == null ? Collections.emptyList()
        : r.getDetails().stream().map(pd -> {
          PayrollDetailDto dto = new PayrollDetailDto();
          dto.setType(pd.getType());
          dto.setDescription(pd.getDescription());
          dto.setAmount(pd.getAmount());
          return dto;
        }).collect(Collectors.toList());
    summary.setDetails(outDetails);
    return summary;
  }

  private PayrollDetailDto createDetail(String type, String description, BigDecimal amount) {
    PayrollDetailDto d = new PayrollDetailDto();
    d.setType(type);
    d.setDescription(description);
    d.setAmount(amount);
    return d;
  }
}
