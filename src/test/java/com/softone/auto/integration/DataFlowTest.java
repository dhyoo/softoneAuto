package com.softone.auto.integration;

import com.softone.auto.model.Company;
import com.softone.auto.service.CompanyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 데이터 흐름 통합 테스트
 * SampleDataInitializer -> CompanyService -> Repository -> SQLite -> 조회
 */
public class DataFlowTest {
    
    @Test
    void testCompleteDataFlow() {
        System.out.println("\n========================================");
        System.out.println("데이터 흐름 통합 테스트 시작");
        System.out.println("========================================\n");
        
        try {
            // 1. CompanyService 생성 (새로운 Repository 인스턴스)
            System.out.println("[1단계] CompanyService 생성");
            CompanyService service1 = new CompanyService();
            System.out.println("  ✓ CompanyService 생성 완료\n");
            
            // 2. 회사 데이터 생성
            System.out.println("[2단계] 회사 데이터 생성");
            Company company = service1.createCompany(
                "테스트 회사",
                "테스트 프로젝트",
                "파견",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "통합 테스트용"
            );
            System.out.println("  ✓ 회사 생성 완료: " + company.getName() + " (ID: " + company.getId() + ")\n");
            
            // 3. 같은 Service로 조회
            System.out.println("[3단계] 같은 Service로 조회");
            List<Company> all1 = service1.getAllCompanies();
            System.out.println("  → 조회된 회사 수: " + all1.size());
            assertFalse(all1.isEmpty(), "회사 목록이 비어있습니다!");
            assertEquals(1, all1.size(), "회사가 1개여야 합니다");
            System.out.println("  ✓ 같은 Service로 조회 성공\n");
            
            // 4. 새로운 Service 인스턴스로 조회 (다른 Connection)
            System.out.println("[4단계] 새로운 Service 인스턴스로 조회 (다른 Connection)");
            CompanyService service2 = new CompanyService();
            List<Company> all2 = service2.getAllCompanies();
            System.out.println("  → 조회된 회사 수: " + all2.size());
            
            if (all2.isEmpty()) {
                System.err.println("  ✗ 새로운 Service에서 데이터를 찾을 수 없습니다!");
                System.err.println("  → Connection 격리 문제일 수 있습니다.");
                System.err.println("  → WAL 모드 설정을 확인하거나 Connection 공유가 필요합니다.");
                fail("새로운 Service 인스턴스에서 데이터를 조회할 수 없습니다");
            } else {
                System.out.println("  ✓ 새로운 Service로 조회 성공\n");
            }
            
            // 5. 활성 회사 조회
            System.out.println("[5단계] 활성 회사 조회");
            List<Company> active = service2.getActiveCompanies();
            System.out.println("  → 조회된 활성 회사 수: " + active.size());
            assertFalse(active.isEmpty(), "활성 회사 목록이 비어있습니다!");
            System.out.println("  ✓ 활성 회사 조회 성공\n");
            
            System.out.println("========================================");
            System.out.println("데이터 흐름 통합 테스트 성공!");
            System.out.println("========================================\n");
            
        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("데이터 흐름 통합 테스트 실패!");
            System.err.println("========================================");
            System.err.println("오류: " + e.getMessage());
            e.printStackTrace();
            fail("테스트 실패: " + e.getMessage());
        }
    }
    
    @Test
    void testMultipleServices() {
        System.out.println("\n========================================");
        System.out.println("다중 Service 인스턴스 테스트 시작");
        System.out.println("========================================\n");
        
        try {
            // Service 1: 데이터 생성
            CompanyService service1 = new CompanyService();
            service1.createCompany("회사 A", "프로젝트 A", "파견", 
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "테스트");
            service1.createCompany("회사 B", "프로젝트 B", "용역", 
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "테스트");
            
            System.out.println("Service 1에서 2개 회사 생성 완료\n");
            
            // Service 2: 데이터 조회
            CompanyService service2 = new CompanyService();
            List<Company> all = service2.getAllCompanies();
            
            System.out.println("Service 2에서 조회된 회사 수: " + all.size());
            
            if (all.size() < 2) {
                System.err.println("  ✗ 예상: 2개, 실제: " + all.size());
                System.err.println("  → Connection 격리 문제로 보입니다.");
                fail("다른 Service 인스턴스에서 데이터를 제대로 조회할 수 없습니다");
            } else {
                System.out.println("  ✓ 다중 Service 인스턴스 테스트 성공\n");
            }
            
        } catch (Exception e) {
            System.err.println("다중 Service 인스턴스 테스트 실패: " + e.getMessage());
            e.printStackTrace();
            fail("테스트 실패: " + e.getMessage());
        }
    }
}

