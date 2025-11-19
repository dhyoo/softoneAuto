package com.softone.auto.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.softone.auto.model.WeeklyReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PDF 보고서 생성 유틸리티
 */
public class PdfReportGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // 한글 폰트 설정
    private static BaseFont baseFont;
    private static Font titleFont;
    private static Font headerFont;
    private static Font labelFont;
    private static Font normalFont;
    private static Font boldFont;
    
    static {
        try {
            // Windows 기본 한글 폰트 시도
            String[] fontPaths = {
                "c:/windows/fonts/malgun.ttf",      // 맑은 고딕
                "c:/windows/fonts/gulim.ttc",       // 굴림
                "c:/windows/fonts/batang.ttc",      // 바탕
                "/usr/share/fonts/truetype/nanum/NanumGothic.ttf",  // Linux
                "/System/Library/Fonts/AppleGothic.ttf"              // Mac
            };
            
            BaseFont font = null;
            for (String path : fontPaths) {
                try {
                    font = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    System.out.println("PDF 폰트 로드 성공: " + path);
                    break;
                } catch (Exception e) {
                    // 다음 폰트 시도
                }
            }
            
            if (font == null) {
                // 기본 폰트 사용
                font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                System.out.println("기본 폰트 사용");
            }
            
            baseFont = font;
            titleFont = new Font(baseFont, 20, Font.BOLD);
            headerFont = new Font(baseFont, 14, Font.BOLD);
            labelFont = new Font(baseFont, 11, Font.BOLD);
            normalFont = new Font(baseFont, 10, Font.NORMAL);
            boldFont = new Font(baseFont, 11, Font.BOLD);
            
        } catch (Exception e) {
            System.err.println("폰트 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 주간보고서를 PDF 파일로 생성
     */
    public static String generateWeeklyReport(WeeklyReport report) {
        try {
            // AppConfig를 통해 설정된 경로 사용
            String dataPath = com.softone.auto.util.AppConfig.getInstance().getOrSelectDataPath();
            
            // reports 디렉토리는 dataPath와 같은 레벨에 생성
            // 예: C:\Users\...\SoftOneAutoData\reports\pdf
            File dataPathFile = new File(dataPath);
            File reportsDir = new File(dataPathFile.getParent(), "reports" + File.separator + "pdf");
            
            // 디렉토리가 없으면 생성
            if (!reportsDir.exists()) {
                if (!reportsDir.mkdirs()) {
                    // 실패 시 dataPath 아래에 직접 생성
                    reportsDir = new File(dataPath, "reports" + File.separator + "pdf");
                    if (!reportsDir.exists()) {
                        if (!reportsDir.mkdirs()) {
                            throw new IOException("reports 디렉토리 생성 실패: " + reportsDir.getAbsolutePath());
                        }
                    }
                }
            }
            
            // 파일 저장 (날짜와 시간 포함: yyyyMMddHHmm)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String fileName = new File(reportsDir, "주간보고서_" + timestamp + ".pdf").getAbsolutePath();
            
            // 파일명 검증 (Path Traversal 방지)
            File file = new File(fileName);
            if (!file.getCanonicalPath().startsWith(reportsDir.getCanonicalPath())) {
                throw new SecurityException("안전하지 않은 파일 경로입니다.");
            }
            
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            
            // 제목
            Paragraph title = new Paragraph("주간 업무 보고서", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30f);
            document.add(title);
            
            // 프로젝트 정보 테이블
            document.add(createInfoTable(report));
            document.add(Chunk.NEWLINE);
            
            // 금주 주요 수행 업무
            Paragraph thisWeekHeader = new Paragraph("금주 주요 수행 업무", headerFont);
            thisWeekHeader.setSpacingBefore(10f);
            thisWeekHeader.setSpacingAfter(10f);
            document.add(thisWeekHeader);
            
            Paragraph thisWeekStats = new Paragraph("요청: " + report.getThisWeekRequestCount() + 
                    " / 완료: " + report.getThisWeekCompleteCount(), normalFont);
            thisWeekStats.setSpacingAfter(5f);
            document.add(thisWeekStats);
            
            // 업무 내용 (텍스트 그대로 출력)
            if (report.getThisWeekTasksText() != null && !report.getThisWeekTasksText().isEmpty()) {
                Paragraph tasksPara = new Paragraph(report.getThisWeekTasksText(), normalFont);
                tasksPara.setIndentationLeft(15f);
                tasksPara.setSpacingAfter(10f);
                document.add(tasksPara);
            }
            document.add(Chunk.NEWLINE);
            
            // 차주 주요 수행 계획
            Paragraph nextWeekHeader = new Paragraph("차주 주요 수행 계획", headerFont);
            nextWeekHeader.setSpacingBefore(10f);
            nextWeekHeader.setSpacingAfter(10f);
            document.add(nextWeekHeader);
            
            Paragraph nextWeekStats = new Paragraph("요청: " + report.getNextWeekRequestCount() + 
                    " / 완료: " + report.getNextWeekCompleteCount(), normalFont);
            nextWeekStats.setSpacingAfter(5f);
            document.add(nextWeekStats);
            
            // 계획 내용 (텍스트 그대로 출력)
            if (report.getNextWeekTasksText() != null && !report.getNextWeekTasksText().isEmpty()) {
                Paragraph plansPara = new Paragraph(report.getNextWeekTasksText(), normalFont);
                plansPara.setIndentationLeft(15f);
                plansPara.setSpacingAfter(10f);
                document.add(plansPara);
            }
            document.add(Chunk.NEWLINE);
            
            // 주요 ISSUE 사항
            Paragraph issueHeader = new Paragraph("주요 ISSUE 사항", headerFont);
            issueHeader.setSpacingBefore(10f);
            issueHeader.setSpacingAfter(10f);
            document.add(issueHeader);
            
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
                                Paragraph contentPara = new Paragraph(content, normalFont);
                                contentPara.setIndentationLeft(15f);
                                contentPara.setSpacingAfter(5f);
                                document.add(contentPara);
                            }
                            currentSectionContent = new StringBuilder();
                            document.add(Chunk.NEWLINE);  // 섹션 간 빈 행 추가
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
                        Paragraph contentPara = new Paragraph(content, normalFont);
                        contentPara.setIndentationLeft(15f);
                        contentPara.setSpacingAfter(5f);
                        document.add(contentPara);
                    }
                }
            }
            document.add(Chunk.NEWLINE);
            
            // 주요 10가지 체크 사항
            Paragraph checkHeader = new Paragraph("주요 10가지 Check 사항", headerFont);
            checkHeader.setSpacingBefore(15f);
            checkHeader.setSpacingAfter(10f);
            document.add(checkHeader);
            
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
                
                Paragraph checkPara = new Paragraph();
                Chunk checkbox = new Chunk(checked ? "☑ " : "☐ ", normalFont);
                Chunk itemText = new Chunk(checkItemNames[i], normalFont);
                checkPara.add(checkbox);
                checkPara.add(itemText);
                checkPara.setSpacingAfter(3f);
                document.add(checkPara);
            }
            
            document.close();
            System.out.println("주간보고서 PDF 파일 생성 완료: " + fileName);
            return fileName;
            
        } catch (Exception e) {
            System.err.println("주간보고서 PDF 생성 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 프로젝트 정보 테이블 생성
     */
    private static PdfPTable createInfoTable(WeeklyReport report) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{25, 75});
        
        // 배경색 정의 (연한 회색)
        BaseColor lightGray = new BaseColor(220, 220, 220);
        
        addInfoRow(table, "프로젝트명", report.getProjectName(), lightGray);
        addInfoRow(table, "보고 기간", 
                report.getStartDate().format(DATE_FORMATTER) + " ~ " + 
                report.getEndDate().format(DATE_FORMATTER), lightGray);
        addInfoRow(table, "작성자", report.getReporter(), lightGray);
        addInfoRow(table, "작성일", report.getCreatedDate().format(DATE_FORMATTER), lightGray);
        
        return table;
    }
    
    /**
     * 정보 행 추가 (레이블은 회색 배경, 값은 흰색 배경)
     */
    private static void addInfoRow(PdfPTable table, String label, String value, BaseColor labelBgColor) {
        // 레이블 셀 (회색 배경)
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(labelBgColor);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        labelCell.setBorderWidth(1);
        labelCell.setBorderColor(BaseColor.GRAY);
        table.addCell(labelCell);
        
        // 값 셀 (흰색 배경)
        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setPadding(8);
        valueCell.setBackgroundColor(BaseColor.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valueCell.setBorderWidth(1);
        valueCell.setBorderColor(BaseColor.GRAY);
        table.addCell(valueCell);
    }
}
