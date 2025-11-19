package com.softone.auto.util;

import org.apache.poi.ss.usermodel.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Excel 스타일 캐싱 유틸리티
 * 동일한 스타일 객체를 재사용하여 메모리 사용량 감소 및 성능 향상
 * 
 * 주의: Workbook별로 스타일을 관리하므로, Workbook이 변경되면 캐시를 초기화해야 함
 */
public class ExcelStyleCache {
    
    // Workbook별 스타일 캐시
    private static final Map<Workbook, Map<String, CellStyle>> workbookStyleCache = new ConcurrentHashMap<>();
    
    /**
     * 제목 스타일 가져오기 (캐싱)
     */
    public static CellStyle getTitleStyle(Workbook workbook) {
        return getOrCreateStyle(workbook, "title", () -> createTitleStyle(workbook));
    }
    
    /**
     * 헤더 스타일 가져오기 (캐싱)
     */
    public static CellStyle getHeaderStyle(Workbook workbook) {
        return getOrCreateStyle(workbook, "header", () -> createHeaderStyle(workbook));
    }
    
    /**
     * 레이블 스타일 가져오기 (캐싱)
     */
    public static CellStyle getLabelStyle(Workbook workbook) {
        return getOrCreateStyle(workbook, "label", () -> createLabelStyle(workbook));
    }
    
    /**
     * 내용 스타일 가져오기 (캐싱)
     */
    public static CellStyle getContentStyle(Workbook workbook) {
        return getOrCreateStyle(workbook, "content", () -> createContentStyle(workbook));
    }
    
    /**
     * 섹션 헤더 스타일 가져오기 (캐싱)
     */
    public static CellStyle getSectionHeaderStyle(Workbook workbook) {
        return getOrCreateStyle(workbook, "sectionHeader", () -> createSectionHeaderStyle(workbook));
    }
    
    /**
     * 스타일 가져오기 또는 생성 (캐싱)
     */
    private static CellStyle getOrCreateStyle(Workbook workbook, String styleName, StyleFactory factory) {
        Map<String, CellStyle> styles = workbookStyleCache.computeIfAbsent(
            workbook, 
            k -> new ConcurrentHashMap<>()
        );
        
        return styles.computeIfAbsent(styleName, k -> factory.create());
    }
    
    /**
     * Workbook별 캐시 초기화
     */
    public static void clearCache(Workbook workbook) {
        workbookStyleCache.remove(workbook);
    }
    
    /**
     * 모든 캐시 초기화
     */
    public static void clearAllCache() {
        workbookStyleCache.clear();
    }
    
    /**
     * 스타일 생성 팩토리 인터페이스
     */
    @FunctionalInterface
    private interface StyleFactory {
        CellStyle create();
    }
    
    /**
     * 제목 스타일 생성
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 헤더 스타일 생성
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 레이블 스타일 생성
     */
    private static CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 내용 스타일 생성
     */
    private static CellStyle createContentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 섹션 헤더 스타일 생성
     */
    private static CellStyle createSectionHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}

