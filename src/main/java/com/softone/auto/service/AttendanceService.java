package com.softone.auto.service;

import com.softone.auto.model.Attendance;
import com.softone.auto.model.Company;
import com.softone.auto.repository.AttendanceRepository;
import com.softone.auto.util.AppContext;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 근태 관리 서비스 (회사별 데이터 분리)
 */
public class AttendanceService {
    
    private final AttendanceRepository repository;
    
    public AttendanceService() {
        this.repository = new AttendanceRepository();
    }
    
    /**
     * 현재 회사의 근태 목록 조회
     */
    public List<Attendance> getAllAttendance() {
        try {
            Company currentCompany = AppContext.getInstance().getCurrentCompany();
            List<Attendance> allAttendances = repository.findAll();
            
            if (currentCompany == null) {
                return allAttendances != null ? allAttendances : new ArrayList<>();
            }
            
            return allAttendances.stream()
                    .filter(att -> currentCompany.getId().equals(att.getCompanyId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("근태 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
            // 예외 발생 시 빈 리스트 반환 (애플리케이션 시작 방해 방지)
            return new ArrayList<>();
        }
    }
    
    /**
     * 근태 등록 (현재 회사에 자동 할당)
     */
    public Attendance createAttendance(String developerId, String developerName, 
                                      LocalDate date, LocalTime checkIn, 
                                      LocalTime checkOut, String type, String notes) {
        // 현재 선택된 회사 ID 자동 설정
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            throw new IllegalStateException("회사가 선택되지 않았습니다. 근태를 등록하려면 먼저 회사를 선택해주세요.");
        }
        
        // 현재 회사 내에서 같은 개발자의 같은 날짜 근태 중복 체크
        // 단, 신규 생성이므로 기존 ID는 없음 (수정 시에는 updateAttendance 사용)
        List<Attendance> existingAttendances = repository.findByDeveloperAndDateRange(
            developerId, date, date);
        
        // 현재 회사의 근태만 필터링 (다른 회사 근태 제외)
        existingAttendances = existingAttendances.stream()
                .filter(att -> currentCompany.getId().equals(att.getCompanyId()))
                .collect(java.util.stream.Collectors.toList());
        
        if (!existingAttendances.isEmpty()) {
            throw new IllegalStateException(
                "해당 날짜(" + date + ")에 이미 근태 기록이 존재합니다.\n" +
                "기존 근태를 수정하거나 다른 날짜를 선택해주세요.");
        }
        
        Attendance attendance = new Attendance();
        attendance.setId(UUID.randomUUID().toString());
        attendance.setCompanyId(currentCompany.getId());
        
        attendance.setDeveloperId(developerId);
        attendance.setDeveloperName(developerName);
        attendance.setDate(date);
        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);
        attendance.setType(type);
        attendance.setNotes(notes);
        
        // 근무 시간 계산
        if (checkIn != null && checkOut != null) {
            long minutes = Duration.between(checkIn, checkOut).toMinutes();
            attendance.setWorkMinutes((int) minutes);
        }
        
        repository.save(attendance);
        return attendance;
    }
    
    /**
     * 근태 수정
     */
    public void updateAttendance(Attendance attendance) {
        // 날짜나 개발자가 변경된 경우 중복 체크 (현재 근태 ID 제외)
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany != null && attendance.getDeveloperId() != null && attendance.getDate() != null) {
            // 같은 개발자의 같은 날짜 근태 조회
            List<Attendance> existingAttendances = repository.findByDeveloperAndDateRange(
                attendance.getDeveloperId(), attendance.getDate(), attendance.getDate());
            
            // 현재 회사의 근태만 필터링하고, 현재 수정 중인 근태는 제외
            existingAttendances = existingAttendances.stream()
                    .filter(att -> currentCompany.getId().equals(att.getCompanyId()))
                    .filter(att -> !att.getId().equals(attendance.getId()))  // 현재 수정 중인 근태 제외
                    .collect(java.util.stream.Collectors.toList());
            
            if (!existingAttendances.isEmpty()) {
                throw new IllegalStateException(
                    "해당 날짜(" + attendance.getDate() + ")에 이미 다른 근태 기록이 존재합니다.\n" +
                    "다른 날짜를 선택해주세요.");
            }
        }
        
        // 근무 시간 재계산
        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long minutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            attendance.setWorkMinutes((int) minutes);
        }
        repository.update(attendance);
    }
    
    /**
     * 근태 삭제
     */
    public void deleteAttendance(String id) {
        repository.delete(id);
    }
    
    /**
     * 특정 개발자의 근태 조회
     */
    public List<Attendance> getAttendanceByDeveloper(String developerId) {
        return repository.findByDeveloperId(developerId);
    }
    
    /**
     * 현재 회사의 특정 기간 근태 조회
     */
    public List<Attendance> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        List<Attendance> attendances = repository.findByDateRange(startDate, endDate);
        
        if (currentCompany == null) {
            return attendances;
        }
        
        return attendances.stream()
                .filter(att -> currentCompany.getId().equals(att.getCompanyId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 개발자별 주간 근태 요약
     */
    public Map<String, Long> getWeeklySummary(LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances = repository.findByDateRange(startDate, endDate);
        
        return attendances.stream()
                .collect(Collectors.groupingBy(
                        Attendance::getDeveloperName,
                        Collectors.counting()
                ));
    }
    
    /**
     * 개발자별 근무 일수 계산
     */
    public long getWorkDays(String developerId, LocalDate startDate, LocalDate endDate) {
        return repository.findByDeveloperAndDateRange(developerId, startDate, endDate)
                .stream()
                .filter(att -> "NORMAL".equals(att.getType()) || "LATE".equals(att.getType()))
                .count();
    }
    
    /**
     * 개발자별 지각 일수 계산
     */
    public long getLateDays(String developerId, LocalDate startDate, LocalDate endDate) {
        return repository.findByDeveloperAndDateRange(developerId, startDate, endDate)
                .stream()
                .filter(att -> "LATE".equals(att.getType()))
                .count();
    }
    
    /**
     * 개발자별 휴가 일수 계산
     */
    public long getVacationDays(String developerId, LocalDate startDate, LocalDate endDate) {
        return repository.findByDeveloperAndDateRange(developerId, startDate, endDate)
                .stream()
                .filter(att -> "VACATION".equals(att.getType()) || "SICK_LEAVE".equals(att.getType()))
                .count();
    }
}

