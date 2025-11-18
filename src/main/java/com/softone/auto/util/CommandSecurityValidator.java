package com.softone.auto.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * OS 명령어 보안 검증 유틸리티
 * OS Command Injection 공격을 방지하기 위한 강력한 검증 제공
 * 
 * 보안 검증 항목:
 * - 명령어 분리자 차단 (;, |, &, &&, ||)
 * - 리다이렉션 문자 차단 (>, <, >>)
 * - 백틱 및 명령 치환 차단 (`)
 * - 화이트리스트 기반 허용 명령어만 실행
 */
@Slf4j
public class CommandSecurityValidator {
    
    // 명령어 분리자 패턴
    private static final Pattern COMMAND_SEPARATOR_PATTERN = Pattern.compile(
        "[;&|]|&&|\\|\\|"
    );
    
    // 리다이렉션 패턴
    private static final Pattern REDIRECTION_PATTERN = Pattern.compile(
        "[<>]|>>|<<"
    );
    
    // 백틱 및 명령 치환 패턴
    private static final Pattern COMMAND_SUBSTITUTION_PATTERN = Pattern.compile(
        "`|\\$\\([^)]+\\)"
    );
    
    // 위험한 문자 패턴
    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile(
        "[`$(){}[\\]\\\\]"
    );
    
    // 허용된 명령어 화이트리스트 (Windows)
    private static final Set<String> ALLOWED_COMMANDS_WINDOWS = new HashSet<>(Arrays.asList(
        "tasklist", "taskkill", "dir", "cd", "type", "echo"
    ));
    
    // 허용된 명령어 화이트리스트 (Linux/Mac)
    private static final Set<String> ALLOWED_COMMANDS_UNIX = new HashSet<>(Arrays.asList(
        "ls", "ps", "kill", "cat", "echo", "pwd"
    ));
    
    /**
     * 명령어가 안전한지 검증
     * 
     * @param command 검증할 명령어
     * @return 안전하면 true, 위험하면 false
     */
    public static boolean isValidCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        // 1. 명령어 분리자 검사
        if (COMMAND_SEPARATOR_PATTERN.matcher(command).find()) {
            log.warn("명령어 분리자 발견: {}", command);
            return false;
        }
        
        // 2. 리다이렉션 검사
        if (REDIRECTION_PATTERN.matcher(command).find()) {
            log.warn("리다이렉션 문자 발견: {}", command);
            return false;
        }
        
        // 3. 명령 치환 검사
        if (COMMAND_SUBSTITUTION_PATTERN.matcher(command).find()) {
            log.warn("명령 치환 패턴 발견: {}", command);
            return false;
        }
        
        // 4. 위험한 문자 검사
        if (DANGEROUS_CHARS_PATTERN.matcher(command).find()) {
            log.warn("위험한 문자가 포함된 명령어: {}", command);
            return false;
        }
        
        return true;
    }
    
    /**
     * 명령어 배열이 안전한지 검증
     * ProcessBuilder에 전달할 명령어 배열 검증
     * 
     * @param commands 검증할 명령어 배열
     * @return 안전하면 true
     */
    public static boolean isValidCommandArray(String[] commands) {
        if (commands == null || commands.length == 0) {
            return false;
        }
        
        // 첫 번째 요소는 명령어 이름이어야 함
        String commandName = commands[0];
        if (commandName == null || commandName.trim().isEmpty()) {
            return false;
        }
        
        // 명령어 이름이 화이트리스트에 있는지 확인
        String os = System.getProperty("os.name").toLowerCase();
        Set<String> allowedCommands = os.contains("win") 
            ? ALLOWED_COMMANDS_WINDOWS 
            : ALLOWED_COMMANDS_UNIX;
        
        if (!allowedCommands.contains(commandName.toLowerCase())) {
            log.warn("허용되지 않은 명령어: {}", commandName);
            return false;
        }
        
        // 모든 인자 검증
        for (String arg : commands) {
            if (!isValidCommandArgument(arg)) {
                log.warn("안전하지 않은 명령어 인자: {}", arg);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 명령어 인자가 안전한지 검증
     * 
     * @param argument 검증할 인자
     * @return 안전하면 true
     */
    public static boolean isValidCommandArgument(String argument) {
        if (argument == null) {
            return true; // null은 허용 (빈 인자)
        }
        
        // 명령어 분리자 검사
        if (COMMAND_SEPARATOR_PATTERN.matcher(argument).find()) {
            return false;
        }
        
        // 리다이렉션 검사
        if (REDIRECTION_PATTERN.matcher(argument).find()) {
            return false;
        }
        
        // 명령 치환 검사
        if (COMMAND_SUBSTITUTION_PATTERN.matcher(argument).find()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 명령어 정제 (위험한 문자 제거)
     * 
     * @param command 정제할 명령어
     * @return 정제된 명령어, 정제 불가능하면 null
     */
    public static String sanitizeCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return null;
        }
        
        // 명령어 분리자나 리다이렉션이 있으면 정제 불가
        if (COMMAND_SEPARATOR_PATTERN.matcher(command).find() ||
            REDIRECTION_PATTERN.matcher(command).find() ||
            COMMAND_SUBSTITUTION_PATTERN.matcher(command).find()) {
            log.warn("명령어에 위험한 패턴이 있어 정제 불가: {}", command);
            return null;
        }
        
        // 위험한 문자 제거
        return DANGEROUS_CHARS_PATTERN.matcher(command).replaceAll("");
    }
    
    /**
     * 명령어 검증 및 예외 발생
     */
    public static void validateCommandOrThrow(String command) throws SecurityException {
        if (!isValidCommand(command)) {
            throw new SecurityException("안전하지 않은 명령어: " + command);
        }
    }
    
    /**
     * 명령어 배열 검증 및 예외 발생
     */
    public static void validateCommandArrayOrThrow(String[] commands) throws SecurityException {
        if (!isValidCommandArray(commands)) {
            throw new SecurityException("안전하지 않은 명령어 배열: " + Arrays.toString(commands));
        }
    }
    
    /**
     * 명령어를 안전하게 실행하기 위한 래퍼
     * ProcessBuilder에 전달하기 전에 검증
     */
    public static String[] sanitizeCommandArray(String[] commands) throws SecurityException {
        if (commands == null || commands.length == 0) {
            throw new SecurityException("명령어 배열이 비어있습니다");
        }
        
        // 검증
        validateCommandArrayOrThrow(commands);
        
        // 정제된 배열 반환
        String[] sanitized = new String[commands.length];
        for (int i = 0; i < commands.length; i++) {
            String sanitizedArg = sanitizeCommand(commands[i]);
            if (sanitizedArg == null) {
                throw new SecurityException("명령어 인자 정제 실패: " + commands[i]);
            }
            sanitized[i] = sanitizedArg;
        }
        
        return sanitized;
    }
}

