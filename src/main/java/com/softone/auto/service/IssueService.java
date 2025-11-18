package com.softone.auto.service;

import com.softone.auto.model.Company;
import com.softone.auto.model.Issue;
import com.softone.auto.repository.IssueRepository;
import com.softone.auto.util.AppContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 이슈 관리 서비스 (회사별 데이터 분리)
 */
public class IssueService {
    
    private final IssueRepository repository;
    
    public IssueService() {
        this.repository = new IssueRepository();
    }
    
    /**
     * 현재 회사의 이슈 목록 조회
     */
    public List<Issue> getAllIssues() {
        try {
            Company currentCompany = AppContext.getInstance().getCurrentCompany();
            List<Issue> allIssues = repository.findAll();
            
            if (currentCompany == null) {
                return allIssues;
            }
            
            return allIssues.stream()
                    .filter(issue -> currentCompany.getId().equals(issue.getCompanyId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("이슈 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 이슈 등록 (현재 회사에 자동 할당)
     */
    public Issue createIssue(String title, String description, String category, 
                           String severity, String reporter, String assignee, String notes) {
        Issue issue = new Issue();
        issue.setId(UUID.randomUUID().toString());
        
        // 현재 선택된 회사 ID 자동 설정
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            throw new IllegalStateException("회사가 선택되지 않았습니다. 이슈를 생성하려면 먼저 회사를 선택해주세요.");
        }
        issue.setCompanyId(currentCompany.getId());
        
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setCategory(category);
        issue.setSeverity(severity);
        issue.setStatus("OPEN");
        issue.setReporter(reporter);
        issue.setAssignee(assignee);
        issue.setCreatedDate(LocalDateTime.now());
        issue.setNotes(notes);
        
        repository.save(issue);
        return issue;
    }
    
    /**
     * 이슈 수정
     */
    public void updateIssue(Issue issue) {
        issue.setUpdatedDate(LocalDateTime.now());
        
        // 해결 상태로 변경 시 해결일 설정
        if ("RESOLVED".equals(issue.getStatus()) && issue.getResolvedDate() == null) {
            issue.setResolvedDate(LocalDateTime.now());
        }
        
        repository.update(issue);
    }
    
    /**
     * 이슈 삭제
     */
    public void deleteIssue(String id) {
        repository.delete(id);
    }
    
    /**
     * 상태별 이슈 조회
     */
    public List<Issue> getIssuesByStatus(String status) {
        return repository.findByStatus(status);
    }
    
    /**
     * 심각도별 이슈 조회
     */
    public List<Issue> getIssuesBySeverity(String severity) {
        return repository.findBySeverity(severity);
    }
    
    /**
     * 현재 회사의 미해결 이슈 조회
     */
    public List<Issue> getOpenIssues() {
        Company currentCompany = AppContext.getInstance().getCurrentCompany();
        if (currentCompany == null) {
            return repository.findByStatus("OPEN");
        }
        
        return repository.findByStatus("OPEN").stream()
                .filter(issue -> currentCompany.getId().equals(issue.getCompanyId()))
                .collect(Collectors.toList());
    }
}

