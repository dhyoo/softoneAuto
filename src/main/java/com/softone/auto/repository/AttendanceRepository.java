package com.softone.auto.repository;

import com.softone.auto.model.Attendance;
import com.softone.auto.repository.sqlite.AttendanceSqliteRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 근태 정보 저장소 (SQLite로 전환됨)
 * @deprecated JSON 기반 저장소에서 SQLite로 전환됨
 */
@Deprecated
public class AttendanceRepository {
    
    private final AttendanceSqliteRepository sqliteRepository;
    
    public AttendanceRepository() {
        try {
            this.sqliteRepository = new AttendanceSqliteRepository();
            
            // 마이그레이션 자동 실행 (최초 1회) - 예외 발생 시 무시
            try {
                migrateFromJsonIfNeeded();
            } catch (Exception e) {
                System.err.println("마이그레이션 실패 (무시): " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("AttendanceRepository 초기화 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Repository 초기화 실패", e);
        }
    }
    
    /**
     * JSON에서 SQLite로 마이그레이션 (필요시)
     */
    private void migrateFromJsonIfNeeded() {
        try {
            List<Attendance> existing = sqliteRepository.findAll();
            if (existing == null || existing.isEmpty()) {
                // 마이그레이션은 CompanyRepository에서 한 번만 실행
                // 중복 실행 방지
            }
        } catch (Exception e) {
            System.err.println("마이그레이션 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 전체 목록 조회
     */
    public List<Attendance> findAll() {
        return sqliteRepository.findAll();
    }
    
    /**
     * 특정 개발자의 근태 조회
     */
    public List<Attendance> findByDeveloperId(String developerId) {
        return sqliteRepository.findByDeveloperId(developerId);
    }
    
    /**
     * 특정 기간의 근태 조회
     */
    public List<Attendance> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return sqliteRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * 특정 개발자의 특정 기간 근태 조회
     */
    public List<Attendance> findByDeveloperAndDateRange(String developerId, LocalDate startDate, LocalDate endDate) {
        return sqliteRepository.findByDeveloperAndDateRange(developerId, startDate, endDate);
    }
    
    /**
     * 근태 저장
     */
    public void save(Attendance attendance) {
        sqliteRepository.save(attendance);
    }
    
    /**
     * 근태 업데이트
     */
    public void update(Attendance attendance) {
        sqliteRepository.update(attendance);
    }
    
    /**
     * 근태 삭제
     */
    public void delete(String id) {
        sqliteRepository.delete(id);
    }
}

