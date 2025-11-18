package com.softone.auto.ui;

import com.softone.auto.util.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 데이터 저장 경로 선택 다이얼로그
 */
public class DataDirectoryDialog extends JDialog {
    
    private String selectedPath = null;
    private JTextField pathField;
    
    public DataDirectoryDialog(Frame parent) {
        super(parent, "데이터 저장 경로 설정", true);
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(600, 250);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        // 제목
        JLabel titleLabel = new JLabel("🚀 SoftOne Auto Manager 시작");
        titleLabel.setFont(ModernDesign.FONT_HEADING);
        titleLabel.setForeground(ModernDesign.PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 설명
        JLabel descLabel = new JLabel("데이터가 저장될 폴더를 선택해주세요");
        descLabel.setFont(ModernDesign.FONT_BODY);
        descLabel.setForeground(ModernDesign.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(descLabel);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 경로 선택 패널
        JPanel pathPanel = new JPanel(new BorderLayout(10, 0));
        pathPanel.setBackground(ModernDesign.BG_SECONDARY);
        pathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        pathField = ModernDesign.createTextField();
        pathField.setEditable(false);
        pathField.setText(System.getProperty("user.home") + File.separator + "SoftOneData");
        pathPanel.add(pathField, BorderLayout.CENTER);
        
        JButton browseButton = ModernDesign.createSecondaryButton("📁 찾아보기");
        browseButton.addActionListener(e -> selectDirectory());
        pathPanel.add(browseButton, BorderLayout.EAST);
        
        mainPanel.add(pathPanel);
        
        mainPanel.add(Box.createVerticalStrut(30));
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(ModernDesign.BG_SECONDARY);
        
        JButton confirmButton = ModernDesign.createPrimaryButton("✅ 확인");
        confirmButton.addActionListener(e -> confirm());
        buttonPanel.add(confirmButton);
        
        JButton cancelButton = ModernDesign.createSecondaryButton("❌ 종료");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
    }
    
    private void selectDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("데이터 저장 폴더 선택");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            pathField.setText(selectedDir.getAbsolutePath());
        }
    }
    
    private void confirm() {
        String path = pathField.getText();
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "경로를 선택해주세요.", 
                "알림", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 경로 저장
        AppSettings.getInstance().setDataDirectory(path);
        selectedPath = path;
        
        JOptionPane.showMessageDialog(this,
            "데이터 저장 경로가 설정되었습니다.\n" + path,
            "완료",
            JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
    
    private void cancel() {
        int result = JOptionPane.showConfirmDialog(this,
            "경로를 설정하지 않으면 프로그램이 종료됩니다.\n정말 종료하시겠습니까?",
            "확인",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    public String getSelectedPath() {
        return selectedPath;
    }
    
    public static String showDialog(Frame parent) {
        DataDirectoryDialog dialog = new DataDirectoryDialog(parent);
        dialog.setVisible(true);
        return dialog.getSelectedPath();
    }
}

