package com.softone.auto.util;

import com.softone.auto.model.Company;

import java.util.ArrayList;
import java.util.List;

/**
 * 애플리케이션 전역 컨텍스트
 * 현재 선택된 회사 정보를 관리
 */
public class AppContext {
    
    private static AppContext instance;
    private Company currentCompany;
    private List<CompanyChangeListener> listeners = new ArrayList<>();
    
    private AppContext() {
    }
    
    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    /**
     * 현재 선택된 회사 가져오기
     */
    public Company getCurrentCompany() {
        return currentCompany;
    }
    
    /**
     * 현재 회사 설정
     */
    public void setCurrentCompany(Company company) {
        this.currentCompany = company;
        notifyListeners();
    }
    
    /**
     * 회사 변경 리스너 등록
     */
    public void addCompanyChangeListener(CompanyChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 리스너에게 변경 알림
     */
    private void notifyListeners() {
        for (CompanyChangeListener listener : listeners) {
            listener.onCompanyChanged(currentCompany);
        }
    }
    
    /**
     * 회사 변경 리스너 인터페이스
     */
    public interface CompanyChangeListener {
        void onCompanyChanged(Company company);
    }
}

