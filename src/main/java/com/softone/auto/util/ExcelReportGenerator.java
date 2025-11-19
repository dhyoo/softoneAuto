package com.softone.auto.util;

import com.softone.auto.model.WeeklyReport;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Excel 보고서 생성 유틸리티
 */
public class ExcelReportGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 주간보고서를 Excel 파일로 생성
     */
    public static String generateWeeklyReport(WeeklyReport report) {
        try {
            // AppConfig를 통해 설정된 경로 사용
            String dataPath = com.softone.auto.util.AppConfig.getInstance().getOrSelectDataPath();
            
            // reports 디렉토리는 dataPath 하위에 생성
            // 예: C:\Users\...\SoftOneAutoData\reports\excel
            File dataPathFile = new File(dataPath).getAbsoluteFile();
            File reportsDir = new File(dataPathFile, "reports" + File.separator + "excel");
            
            // 디렉토리가 없으면 생성
            if (!reportsDir.exists()) {
                if (!reportsDir.mkdirs()) {
                    throw new IOException("reports 디렉토리 생성 실패: " + reportsDir.getAbsolutePath());
                }
            }
            
            // SXSSFWorkbook 사용 (스트리밍 방식, 메모리 효율적)
            // 100행씩 메모리에 유지, 나머지는 임시 파일에 저장
            Workbook workbook = new SXSSFWorkbook(100);
            Sheet sheet = workbook.createSheet("주간보고서");
            
            // 스타일 생성 (캐싱 사용)
            CellStyle titleStyle = ExcelStyleCache.getTitleStyle(workbook);
            CellStyle headerStyle = ExcelStyleCache.getHeaderStyle(workbook);
            CellStyle labelStyle = ExcelStyleCache.getLabelStyle(workbook);
            CellStyle contentStyle = ExcelStyleCache.getContentStyle(workbook);
            CellStyle sectionHeaderStyle = ExcelStyleCache.getSectionHeaderStyle(workbook);
            
            int rowNum = 0;
            
            // 제목 (A1:F1 병합)
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            for (int i = 0; i < 6; i++) {
                Cell cell = titleRow.createCell(i);
                if (i == 0) {
                    cell.setCellValue("주간 업무 보고서");
                }
                cell.setCellStyle(titleStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            
            // 빈 행
            rowNum++;
            
            // 프로젝트 정보 섹션
            createInfoRow(sheet, rowNum++, "프로젝트명", report.getProjectName(), labelStyle, contentStyle);
            createInfoRow(sheet, rowNum++, "보고 기간", 
                    report.getStartDate().format(DATE_FORMATTER) + " ~ " + 
                    report.getEndDate().format(DATE_FORMATTER), labelStyle, contentStyle);
            createInfoRow(sheet, rowNum++, "작성자", report.getReporter(), labelStyle, contentStyle);
            createInfoRow(sheet, rowNum++, "작성일", report.getCreatedDate().format(DATE_FORMATTER), labelStyle, contentStyle);
            
            // 빈 행
            rowNum++;
            
            // 금주 주요 수행 업무
            createSectionHeader(sheet, rowNum++, "금주 주요 수행 업무", sectionHeaderStyle);
            createStatsRow(sheet, rowNum++, "요청", report.getThisWeekRequestCount(), "완료", report.getThisWeekCompleteCount(), labelStyle, contentStyle);
            
            // 업무 내용 (텍스트 그대로 출력)
            if (report.getThisWeekTasksText() != null && !report.getThisWeekTasksText().isEmpty()) {
                createMergedRow(sheet, rowNum++, 0, 5, report.getThisWeekTasksText(), contentStyle);
            }
            rowNum++;
            
            // 차주 주요 수행 계획
            createSectionHeader(sheet, rowNum++, "차주 주요 수행 계획", sectionHeaderStyle);
            createStatsRow(sheet, rowNum++, "요청", report.getNextWeekRequestCount(), "완료", report.getNextWeekCompleteCount(), labelStyle, contentStyle);
            
            // 계획 내용 (텍스트 그대로 출력)
            if (report.getNextWeekTasksText() != null && !report.getNextWeekTasksText().isEmpty()) {
                createMergedRow(sheet, rowNum++, 0, 5, report.getNextWeekTasksText(), contentStyle);
            }
            rowNum++;
            
            // 주요 ISSUE 사항
            createSectionHeader(sheet, rowNum++, "주요 ISSUE 사항", sectionHeaderStyle);
            if (report.getAdditionalNotes() != null && !report.getAdditionalNotes().isEmpty()) {
                String[] lines = report.getAdditionalNotes().split("\n");
                StringBuilder currentSectionContent = new StringBuilder();
                
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    
                    // 섹션 제목 감지 (■로 시작하는 줄)
                    if (trimmedLine.startsWith("■")) {
                        // 이전 섹션 내용이 있으면 출력
                        if (currentSectionContent.length() > 0) {
                            String content = currentSectionContent.toString().trim();
                            if (!content.isEmpty()) {
                                createMergedRow(sheet, rowNum++, 0, 5, content, contentStyle);
                            }
                            currentSectionContent = new StringBuilder();
                            rowNum++;  // 섹션 간 빈 행 추가
                        }
                        
                        // 새 섹션 시작
                        currentSectionContent.append(trimmedLine);
                        
                    } else {
                        // 모든 줄을 내용으로 처리 (빈 줄, 일반 텍스트, 숫자 등 모두 포함)
                        if (currentSectionContent.length() > 0) {
                            currentSectionContent.append("\n");
                        }
                        
                        // 원본 줄의 앞뒤 공백은 제거하되, 내용은 그대로 유지
                        if (!trimmedLine.isEmpty()) {
                            currentSectionContent.append(trimmedLine);
                        }
                        // 빈 줄은 줄바꿈만 추가됨 (이미 위에서 \n 추가됨)
                    }
                }
                
                // 마지막 섹션 출력
                if (currentSectionContent.length() > 0) {
                    String content = currentSectionContent.toString().trim();
                    if (!content.isEmpty()) {
                        createMergedRow(sheet, rowNum++, 0, 5, content, contentStyle);
                    }
                }
            }
            rowNum++;
            
            // 주요 10가지 체크 사항
            createSectionHeader(sheet, rowNum++, "주요 10가지 Check 사항", sectionHeaderStyle);
            
            String[] checkItemNames = {
                "고객사 보안 규정 준수",
                "품질(납기, 생산성) 양호",
                "불법 S/W 미사용",
                "사고 대응 체계 숙지",
                "고객사 생산성 향상 노력",
                "고객 중심적 업무 수행",
                "회사 대표 의식",
                "성실한 근태",
                "단정한 복장",
                "자기/회사 발전 노력"
            };
            
            for (int i = 0; i < checkItemNames.length; i++) {
                boolean checked = false;
                if (report.getCheckItems() != null && i < report.getCheckItems().size()) {
                    checked = report.getCheckItems().get(i);
                }
                
                Row checkRow = sheet.createRow(rowNum++);
                checkRow.setHeightInPoints(20);
                
                // 체크박스 표시 (☑ or ☐)
                Cell checkCell = checkRow.createCell(0);
                checkCell.setCellValue(checked ? "☑" : "☐");
                checkCell.setCellStyle(labelStyle);
                
                // 항목명
                for (int j = 1; j < 6; j++) {
                    Cell cell = checkRow.createCell(j);
                    if (j == 1) {
                        cell.setCellValue(checkItemNames[i]);
                    }
                    cell.setCellStyle(contentStyle);
                }
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 5));
            }
            
            // 열 너비 설정
            sheet.setColumnWidth(0, 5000);  // A 열 - 레이블
            sheet.setColumnWidth(1, 12000); // B 열 - 내용
            sheet.setColumnWidth(2, 3000);  // C 열
            sheet.setColumnWidth(3, 3000);  // D 열
            sheet.setColumnWidth(4, 3000);  // E 열
            sheet.setColumnWidth(5, 3000);  // F 열
            
            // 파일 저장 (날짜와 시간 포함: yyyyMMddHHmm)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String fileName = new File(reportsDir, "주간보고서_" + timestamp + ".xlsx").getAbsolutePath();
            
            // 파일명 검증 (Path Traversal 방지)
            File file = new File(fileName);
            if (!file.getCanonicalPath().startsWith(reportsDir.getCanonicalPath())) {
                throw new SecurityException("안전하지 않은 파일 경로입니다.");
            }
            
            try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
                workbook.write(fileOut);
                System.out.println("주간보고서 Excel 파일 생성 완료: " + fileName);
            } finally {
                // SXSSFWorkbook의 임시 파일 정리
                if (workbook instanceof SXSSFWorkbook) {
                    ((SXSSFWorkbook) workbook).dispose();
                }
                workbook.close();
                // 스타일 캐시 정리
                ExcelStyleCache.clearCache(workbook);
            }
            
            return fileName;
            
        } catch (Exception e) {
            System.err.println("주간보고서 Excel 생성 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 정보 행 생성 (2열 레이아웃)
     */
    private static void createInfoRow(Sheet sheet, int rowNum, String label, String value, 
                                     CellStyle labelStyle, CellStyle contentStyle) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(20);
        
        // 레이블 (A 열)
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        // 값 (B 열, B:F 병합)
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(contentStyle);
        
        // B:F 병합 (모든 셀에 테두리 적용)
        for (int i = 2; i < 6; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(contentStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 1, 5));
    }
    
    /**
     * 통계 행 생성 (4칸: 요청 | 값 | 완료 | 값)
     */
    private static void createStatsRow(Sheet sheet, int rowNum, 
                                      String label1, Integer value1,
                                      String label2, Integer value2,
                                      CellStyle labelStyle, CellStyle contentStyle) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(20);
        
        // A열: 첫 번째 레이블 (요청)
        Cell cell0 = row.createCell(0);
        cell0.setCellValue(label1);
        cell0.setCellStyle(labelStyle);
        
        // B열: 첫 번째 값
        Cell cell1 = row.createCell(1);
        cell1.setCellValue(value1 != null ? value1 : 0);
        cell1.setCellStyle(contentStyle);
        
        // C열: 두 번째 레이블 (완료)
        Cell cell2 = row.createCell(2);
        cell2.setCellValue(label2);
        cell2.setCellStyle(labelStyle);
        
        // D~F열: 두 번째 값 (병합)
        for (int i = 3; i < 6; i++) {
            Cell cell = row.createCell(i);
            if (i == 3) {
                cell.setCellValue(value2 != null ? value2 : 0);
            }
            cell.setCellStyle(contentStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 3, 5));
    }
    
    /**
     * 섹션 헤더 생성 (A~F 병합, 회색 배경)
     */
    private static void createSectionHeader(Sheet sheet, int rowNum, String title, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(25);
        for (int i = 0; i < 6; i++) {
            Cell cell = row.createCell(i);
            if (i == 0) {
                cell.setCellValue(title);
            }
            cell.setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 5));
    }
    
    /**
     * 병합된 행 생성 (startCol ~ endCol)
     */
    private static void createMergedRow(Sheet sheet, int rowNum, int startCol, int endCol, 
                                       String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        
        // 여러 줄 텍스트면 높이 조정
        int lineCount = value.split("\n").length;
        row.setHeightInPoints(Math.max(20, lineCount * 15));
        
        // 모든 셀 생성 (0부터 endCol까지)
        for (int i = 0; i <= endCol; i++) {
            Cell cell = row.createCell(i);
            if (i == startCol) {
                cell.setCellValue(value);
            }
            cell.setCellStyle(style);
        }
        
        // 병합 영역 설정
        if (startCol < endCol) {
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startCol, endCol));
        }
    }
    
    /**
     * 제목 스타일
     */
    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 섹션 헤더 스타일 (회색 배경)
     */
    private static CellStyle createSectionHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    /**
     * 헤더 스타일
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    /**
     * 레이블 스타일
     */
    private static CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    /**
     * 내용 스타일
     */
    private static CellStyle createContentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("맑은 고딕");
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }
}
